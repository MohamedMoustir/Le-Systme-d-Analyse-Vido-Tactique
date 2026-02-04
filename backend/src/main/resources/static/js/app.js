/**
 * Football Video Analyzer - Frontend Application
 */

// ===== State =====
let stompClient = null;
let isConnected = false;
let currentVideoPath = null;
let isAnalyzing = false;
let originalVideoUrl = null;
let annotatedVideoUrl = null;
let isLiveStreaming = false;
let lastBallHolderId = null;
let lastTimelineFrame = 0;
let lastBallLostFrame = 0;
let videoFps = 30;

// Timeline stability settings
const MIN_FRAMES_BETWEEN_CHANGES = 15;
const MIN_FRAMES_BETWEEN_BALL_LOST = 45; // Minimum ~1.5s between "ball lost" events
const MAX_REALISTIC_SPEED = 40; // Max realistic speed in km/h

// ===== DOM Elements =====
const uploadZone = document.getElementById('uploadZone');
const videoInput = document.getElementById('videoInput');
const videoContainer = document.getElementById('videoContainer');
const videoPlayer = document.getElementById('videoPlayer');
const videoSource = document.getElementById('videoSource');
const startBtn = document.getElementById('startBtn');
const stopBtn = document.getElementById('stopBtn');
const deviceSelect = document.getElementById('deviceSelect');
const statusIndicator = document.getElementById('statusIndicator');
const progressContainer = document.getElementById('progressContainer');
const progressFill = document.getElementById('progressFill');
const progressText = document.getElementById('progressText');
const logsScroll = document.getElementById('logsScroll');
const clearLogsBtn = document.getElementById('clearLogsBtn');

// Stats elements (compact panel)
const playersCount = document.getElementById('playersCount');
const ballStatus = document.getElementById('ballStatus');
const team1Possession = document.getElementById('team1Possession');
const team2Possession = document.getElementById('team2Possession');
const ballHolderCard = document.getElementById('ballHolderCard');
const ballHolderBadge = document.getElementById('ballHolderBadge');
const ballHolderNumber = document.getElementById('ballHolderNumber');
const ballHolderTeam = document.getElementById('ballHolderTeam');
const ballHolderSpeed = document.getElementById('ballHolderSpeed');
const ballHolderDistance = document.getElementById('ballHolderDistance');
const fastestPlayerEl = document.getElementById('fastestPlayer');
const topDistanceEl = document.getElementById('topDistance');

// Timeline elements
const timelinePanel = document.getElementById('timelinePanel');
const timelineScroll = document.getElementById('timelineScroll');
const clearTimelineBtn = document.getElementById('clearTimelineBtn');

// ===== Initialize =====
document.addEventListener('DOMContentLoaded', () => {
    setupUpload();
    setupControls();
    setupTimeline();
    connectWebSocket();
});

// ===== WebSocket Connection =====
function connectWebSocket() {
    const socket = new SockJS('/ws-analysis');
    stompClient = Stomp.over(socket);

    // Disable debug logs
    stompClient.debug = null;

    stompClient.connect({},
        (frame) => {
            isConnected = true;
            addLog('WebSocket connecte', 'success');

            // Subscribe to analysis topic
            stompClient.subscribe('/topic/analysis', (message) => {
                const data = JSON.parse(message.body);
                handleAnalysisMessage(data);
            });
        },
        (error) => {
            isConnected = false;
            addLog('Erreur WebSocket: ' + error, 'error');
            // Try to reconnect after 5 seconds
            setTimeout(connectWebSocket, 5000);
        }
    );
}

// ===== Handle Analysis Messages =====
function handleAnalysisMessage(data) {
    if (!data || !data.type) return;

    switch (data.type) {
        case 'video_info':
            addLog(`Video: ${data.total_frames ?? data.totalFrames} frames, ${data.fps?.toFixed(1)} FPS, ${data.width}x${data.height}`, 'info');
            if (data.annotation_enabled ?? data.annotationEnabled) {
                addLog('Annotation video activee - video annotee sera disponible a la fin', 'info');
            }
            // Store FPS for timeline timestamp calculation
            videoFps = data.fps || 30;
            progressContainer.style.display = 'flex';
            // Set status to analyzing when video info is received
            setStatus('analyzing');
            // Hide empty message when analysis starts
            const emptyMsg = document.getElementById('timelineEmpty');
            if (emptyMsg) emptyMsg.style.display = 'none';
            break;

        case 'warning':
            addLog(data.message, 'warning');
            break;

        case 'analysis_start':
            addLog(data.message || 'Analyse demarree...', 'success');
            setStatus('analyzing');
            // Enable live stream tab (don't start stream again if already started)
            enableLiveStreamTab();
            break;

        case 'frame_analysis':
            updateStats(data);
            // Update timeline with ball holder events
            updateTimeline(data);
            // Log important events
            logFrameEvents(data);
            break;

        case 'progress':
            updateProgress(data.percent, data.frame_num ?? data.frameNum, data.total_frames ?? data.totalFrames);
            break;

        case 'analysis_complete':
            addLog(`Analyse terminee! ${data.total_frames_processed ?? data.totalFramesProcessed} frames traitees`, 'success');
            if (data.final_possession ?? data.finalPossession) {
                const fp = data.final_possession ?? data.finalPossession;
                // Support both "team_1"/"team_2" and "1"/"2" keys
                const t1 = fp.team_1 ?? fp['1'] ?? 50;
                const t2 = fp.team_2 ?? fp['2'] ?? 50;
                addLog(`Possession finale - T1: ${t1.toFixed(1)}% | T2: ${t2.toFixed(1)}%`, 'success');
            }
            // Handle annotated video
            const outputVideo = data.output_video ?? data.outputVideo;
            if (outputVideo) {
                // Extract filename from path and build web URL
                const fileName = outputVideo.split(/[/\\]/).pop();
                annotatedVideoUrl = '/videos/' + fileName;
                enableAnnotatedVideoTab();
                addLog(`Video annotee disponible: ${fileName}`, 'success');
            }
            // Stop live stream
            stopLiveStream();
            setStatus('ready');
            isAnalyzing = false;
            updateButtons();
            progressFill.style.width = '100%';
            progressText.textContent = '100%';
            break;

        case 'error':
            addLog('Erreur: ' + data.message, 'error');
            setStatus('error');
            isAnalyzing = false;
            updateButtons();
            break;

        case 'stopped':
            addLog('Analyse arretee', 'warning');
            setStatus('ready');
            isAnalyzing = false;
            updateButtons();
            break;

        case 'frame_error':
            addLog(`Erreur frame ${data.frame_num ?? data.frameNum}: ${data.error}`, 'warning');
            break;

        case 'annotated_video_ready':
            // Annotated video is ready from live stream
            annotatedVideoUrl = data.url;
            enableAnnotatedVideoTab();
            addLog(`Video annotee disponible: ${data.filename}`, 'success');
            break;
    }
}

// ===== Log Frame Events =====
function logFrameEvents(data) {
    const frameNum = data.frame_num ?? data.frameNum ?? 0;

    // Log every 50 frames with summary
    if (frameNum % 50 === 0) {
        const totalPlayers = data.players_count ?? data.playersCount ?? 0;
        const t1 = data.team_1_count ?? data.team1Count ?? 0;
        const t2 = data.team_2_count ?? data.team2Count ?? 0;
        const ballDetected = data.ball_detected ?? data.ballDetected ? 'OUI' : 'NON';
        const possession = data.possession;

        let msg = `[Frame ${frameNum}] Joueurs: ${totalPlayers} (T1:${t1} T2:${t2}) | Ballon: ${ballDetected}`;
        if (possession) {
            const p1 = possession['1'] ?? possession[1] ?? 50;
            const p2 = possession['2'] ?? possession[2] ?? 50;
            msg += ` | Possession: ${p1.toFixed(0)}%-${p2.toFixed(0)}%`;
        }
        addLog(msg, 'frame');
    }

    // Log ball holder changes
    const ballHolderId = data.ball_holder_id ?? data.ballHolderId;
    const players = data.players ?? [];

    if (ballHolderId && players.length > 0 && frameNum % 30 === 0) {
        const holder = players.find(p => p.id === ballHolderId);
        if (holder) {
            const jerseyNum = holder.jersey_number ?? holder.jerseyNumber;
            const playerName = holder.player_name ?? holder.playerName;
            const teamId = holder.team;
            const speed = holder.speed_kmh ?? holder.speedKmh ?? 0;

            let holderName = playerName || (jerseyNum && jerseyNum <= 99 ? `#${jerseyNum}` : `Joueur`);
            addLog(`Ballon: ${holderName} (Equipe ${teamId}) - ${speed.toFixed(1)} km/h`, 'success');
        }
    }
}

// ===== Update Statistics (Compact Panel) =====
function updateStats(data) {
    // Players count with team breakdown
    const totalPlayers = data.players_count ?? data.playersCount ?? 0;
    const team1Count = data.team_1_count ?? data.team1Count ?? 0;
    const team2Count = data.team_2_count ?? data.team2Count ?? 0;
    playersCount.textContent = `${totalPlayers} (${team1Count}/${team2Count})`;

    // Ball status
    const ballDetected = data.ball_detected ?? data.ballDetected ?? false;
    ballStatus.textContent = ballDetected ? 'Detecte' : 'Non detecte';
    ballStatus.style.color = ballDetected ? 'var(--success-color)' : 'var(--text-secondary)';

    // Possession
    if (data.possession) {
        const t1 = data.possession['1'] ?? data.possession[1] ?? 50;
        const t2 = data.possession['2'] ?? data.possession[2] ?? 50;
        team1Possession.textContent = t1.toFixed(1) + '%';
        team2Possession.textContent = t2.toFixed(1) + '%';
    }

    // Ball holder - support both snake_case and camelCase
    const ballHolderId = data.ball_holder_id ?? data.ballHolderId;
    const players = data.players ?? [];

    // Get team names from data
    const team1NameFromData = data.team_1_name ?? data.team1Name ?? 'Equipe 1';
    const team2NameFromData = data.team_2_name ?? data.team2Name ?? 'Equipe 2';

    if (ballHolderId && players.length > 0) {
        const holder = players.find(p => p.id === ballHolderId);
        if (holder) {
            ballHolderCard.style.display = 'block';

            // Get jersey number and player name
            const jerseyNum = holder.jersey_number ?? holder.jerseyNumber;
            const playerName = holder.player_name ?? holder.playerName;
            const teamName = holder.team === 1 ? team1NameFromData : team2NameFromData;

            // Display player info
            if (jerseyNum) {
                ballHolderNumber.textContent = `#${jerseyNum}`;
            } else {
                ballHolderNumber.textContent = `#${holder.id}`;
            }

            // Show player name if available
            if (playerName) {
                ballHolderTeam.innerHTML = `<strong>${playerName}</strong><br><small>${teamName}</small>`;
            } else {
                ballHolderTeam.textContent = teamName;
            }

            const speed = holder.speed_kmh ?? holder.speedKmh ?? 0;
            const distance = holder.distance_m ?? holder.distanceM ?? 0;
            ballHolderSpeed.textContent = speed.toFixed(1) + ' km/h';
            ballHolderDistance.textContent = distance.toFixed(1) + ' m';

            // Set team color
            ballHolderBadge.className = 'player-badge';
            if (holder.team === 1) {
                ballHolderBadge.classList.add('team1');
            } else if (holder.team === 2) {
                ballHolderBadge.classList.add('team2');
            }
        }
    } else {
        ballHolderCard.style.display = 'none';
    }

    // Update fastest player and top distance (compact)
    if (players.length > 0) {
        updateTopPlayers(players);
    }
}

// ===== Update Top Players Stats (Compact) =====
function updateTopPlayers(players) {
    let fastest = null;
    let maxSpeed = 0;
    let topDistance = null;
    let maxDist = 0;

    players.forEach(p => {
        const speed = p.speed_kmh ?? p.speedKmh ?? 0;
        const dist = p.distance_m ?? p.distanceM ?? 0;

        if (speed > maxSpeed) {
            maxSpeed = speed;
            fastest = p;
        }
        if (dist > maxDist) {
            maxDist = dist;
            topDistance = p;
        }
    });

    function getPlayerDisplay(player) {
        const jerseyNum = player.jersey_number ?? player.jerseyNumber;
        const playerName = player.player_name ?? player.playerName;
        const teamName = player.team === 1 ? 'T1' : 'T2';
        // Only consider valid jersey numbers (1-99)
        const validJersey = jerseyNum && jerseyNum <= 99;

        if (playerName && validJersey) {
            return `${playerName} #${jerseyNum}`;
        } else if (validJersey) {
            return `#${jerseyNum} (${teamName})`;
        } else if (playerName) {
            return `${playerName} (${teamName})`;
        } else {
            return `Joueur (${teamName})`;
        }
    }

    if (fastest && maxSpeed > 0) {
        fastestPlayerEl.style.display = 'block';
        fastestPlayerEl.querySelector('.stat-value').textContent = getPlayerDisplay(fastest);
        fastestPlayerEl.querySelector('.stat-detail').textContent = `${maxSpeed.toFixed(1)} km/h`;
    }

    if (topDistance && maxDist > 0) {
        topDistanceEl.style.display = 'block';
        topDistanceEl.querySelector('.stat-value').textContent = getPlayerDisplay(topDistance);
        topDistanceEl.querySelector('.stat-detail').textContent = `${maxDist.toFixed(1)} m`;
    }
}

// ===== Update Progress =====
function updateProgress(percent, current, total) {
    progressFill.style.width = percent + '%';
    progressText.textContent = percent.toFixed(1) + '%';
}

// ===== Setup Upload =====
function setupUpload() {
    uploadZone.addEventListener('click', () => {
        videoInput.click();
    });

    videoInput.addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
            uploadVideo(e.target.files[0]);
        }
    });

    uploadZone.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadZone.classList.add('dragover');
    });

    uploadZone.addEventListener('dragleave', () => {
        uploadZone.classList.remove('dragover');
    });

    uploadZone.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadZone.classList.remove('dragover');

        if (e.dataTransfer.files.length > 0) {
            uploadVideo(e.dataTransfer.files[0]);
        }
    });
}

// ===== Upload Video =====
// ===== Upload Video =====
async function uploadVideo(file) {
    addLog(`Upload de ${file.name}...`, 'info');

    const formData = new FormData();
    // Backend expects 'file', not 'video'
    formData.append('file', file);

    // Backend expects a JSON part named 'data'
    const jsonData = JSON.stringify({ title: file.name });
    formData.append('data', new Blob([jsonData], { type: 'application/json' }));

    try {
        // Endpoint changed to match VideoController
        const response = await fetch('/api/videos', {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            const result = await response.json();

            // Match the VideoResponse DTO fields
            addLog(`Video uploadee: ${result.titre || file.name}`, 'success');

            // Save the ID and Path
            currentVideoId = result.id; // Crucial for startAnalysis
            currentVideoPath = result.urlFichier;

            uploadZone.style.display = 'none';
            videoContainer.style.display = 'block';

            // Assuming you serve uploads statically
            videoSource.src = `/uploads/${result.urlFichier}`;
            videoPlayer.load();

            resetVideoTabs();
            startBtn.disabled = false;
            setStatus('ready');

            // Connect WS immediately after upload to be ready
            connectWebSocket();

        } else {
            const text = await response.text();
            addLog('Erreur upload: ' + text, 'error');
        }
    } catch (error) {
        addLog('Erreur upload: ' + error.message, 'error');
    }
}
// ===== Setup Controls =====
function setupControls() {
    startBtn.addEventListener('click', startAnalysis);
    stopBtn.addEventListener('click', stopAnalysis);
    clearLogsBtn.addEventListener('click', clearLogs);
}

// ===== Start Analysis =====
async function startAnalysis() {
    if (!currentVideoPath) {
        addLog('Veuillez d\'abord selectionner une video', 'warning');
        return;
    }
    try {
        const response = await fetch(`/api/videos/${currentVideoId}/analyze`, {
            method: 'POST'
        });

        if(response.ok) {
            addLog('Analyse demandée au serveur...', 'success');
            // Start the visual stream
            enableLiveStreamTab();
            showLiveStream();
        } else {
            const err = await response.json();
            addLog('Erreur serveur: ' + err.message, 'error');
            isAnalyzing = false;
            updateButtons();
        }
    } catch(e) {
        addLog('Erreur API: ' + e, 'error');
    }

}

// ===== Stop Analysis =====
async function stopAnalysis() {
    // Stop live stream first
    stopLiveStream();

    // Stop analysis on server
    try {
        await fetch('/stop-analysis', { method: 'POST' });
        await fetch('/stream/stop', { method: 'POST' });
        addLog('Analyse arretee', 'info');
    } catch (error) {
        addLog('Erreur: ' + error.message, 'error');
    }

    // Update state
    isAnalyzing = false;
    updateButtons();
    setStatus('ready');

    // Hide progress
    progressContainer.style.display = 'none';

    // Switch back to original video tab
    const originalTab = document.getElementById('originalVideoTab');
    const liveStreamTab = document.getElementById('liveStreamTab');
    const liveStreamContainer = document.getElementById('liveStreamContainer');

    originalTab.classList.add('active');
    liveStreamTab.classList.remove('active');
    liveStreamContainer.style.display = 'none';
    videoPlayer.style.display = 'block';

    // Load original video
    if (originalVideoUrl) {
        videoSource.src = originalVideoUrl;
        videoPlayer.load();
    }
}

// ===== Update Buttons State =====
function updateButtons() {
    startBtn.disabled = isAnalyzing || !currentVideoPath;
    stopBtn.disabled = !isAnalyzing;
}

// ===== Reset Stats =====
function resetStats() {
    playersCount.textContent = '--';
    ballStatus.textContent = '--';
    team1Possession.textContent = '--%';
    team2Possession.textContent = '--%';
    if (ballHolderCard) ballHolderCard.style.display = 'none';
    // Clear timeline for new analysis
    clearTimeline();
}

// ===== Set Status =====
function setStatus(status) {
    statusIndicator.className = 'status-indicator ' + status;
    const statusText = statusIndicator.querySelector('.status-text');

    switch (status) {
        case 'ready':
            statusText.textContent = 'Pret';
            break;
        case 'analyzing':
            statusText.textContent = 'Analyse en cours...';
            break;
        case 'error':
            statusText.textContent = 'Erreur';
            break;
        default:
            statusText.textContent = 'En attente';
    }
}

// ===== Add Log Entry =====
function addLog(message, type = 'info') {
    const now = new Date();
    const time = now.toLocaleTimeString('fr-FR');

    const entry = document.createElement('div');
    entry.className = 'log-entry ' + type;
    entry.innerHTML = `
        <span class="log-time">${time}</span>
        <span class="log-message">${escapeHtml(message)}</span>
    `;

    logsScroll.appendChild(entry);
    logsScroll.scrollTop = logsScroll.scrollHeight;

    // Keep only last 200 logs
    while (logsScroll.children.length > 200) {
        logsScroll.removeChild(logsScroll.firstChild);
    }
}

// ===== Clear Logs =====
function clearLogs() {
    logsScroll.innerHTML = '';
    addLog('Logs effaces', 'info');
}

// ===== Escape HTML =====
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// ===== Video Tab Functions =====
function showOriginalVideo() {
    if (!originalVideoUrl) return;

    // If analysis is running, ask for confirmation
    if (isAnalyzing || isLiveStreaming) {
        if (!confirm("Arreter l'analyse en cours et revenir a la video originale ?")) {
            return;
        }
        // Stop everything
        stopAnalysisAndStream();
    }

    const originalTab = document.getElementById('originalVideoTab');
    const liveStreamTab = document.getElementById('liveStreamTab');
    const annotatedInfo = document.getElementById('annotatedVideoInfo');
    const liveStreamContainer = document.getElementById('liveStreamContainer');

    // Update tabs
    originalTab.classList.add('active');
    liveStreamTab.classList.remove('active');
    if (annotatedInfo) annotatedInfo.style.display = 'none';

    // Hide live stream, show video player
    liveStreamContainer.style.display = 'none';
    videoPlayer.style.display = 'block';

    // Load original video
    videoSource.src = originalVideoUrl;
    videoPlayer.load();
}

// Stop analysis and live stream
async function stopAnalysisAndStream() {
    // Stop live stream
    stopLiveStream();

    // Stop analysis on server
    try {
        await fetch('/stop-analysis', { method: 'POST' });
        await fetch('/stream/stop', { method: 'POST' });
    } catch (e) {
        // Ignore errors
    }

    // Update state
    isAnalyzing = false;
    updateButtons();
    setStatus('ready');

    // Hide progress
    progressContainer.style.display = 'none';

    addLog('Analyse arretee - retour a la video originale', 'info');
}

function showAnnotatedVideo() {
    // Feature disabled - no annotated video saving
    return;
}

function enableAnnotatedVideoTab() {
    // Feature disabled - no annotated video saving
    return;
}

function resetVideoTabs() {
    const originalTab = document.getElementById('originalVideoTab');
    const liveStreamTab = document.getElementById('liveStreamTab');
    const annotatedInfo = document.getElementById('annotatedVideoInfo');

    originalTab.classList.add('active');
    liveStreamTab.classList.remove('active');
    liveStreamTab.disabled = true;
    if (annotatedInfo) annotatedInfo.style.display = 'none';
    annotatedVideoUrl = null;

    // Stop any active stream
    stopLiveStream();
}

// ===== Live Stream Functions =====
function enableLiveStreamTab() {
    const liveStreamTab = document.getElementById('liveStreamTab');
    liveStreamTab.disabled = false;
    liveStreamTab.title = 'Voir l\'analyse en temps reel';
}

function showLiveStream() {
    if (!currentVideoPath) return;

    // Start live stream if not already streaming
    if (!isLiveStreaming) {
        startLiveStream();
    }

    const originalTab = document.getElementById('originalVideoTab');
    const liveStreamTab = document.getElementById('liveStreamTab');
    const annotatedInfo = document.getElementById('annotatedVideoInfo');
    const liveStreamContainer = document.getElementById('liveStreamContainer');

    // Update tabs
    originalTab.classList.remove('active');
    liveStreamTab.classList.add('active');
    if (annotatedInfo) annotatedInfo.style.display = 'block';

    // Show stream, hide video player
    videoPlayer.style.display = 'none';
    liveStreamContainer.style.display = 'block';

    addLog('Affichage du live stream', 'info');
}

function startLiveStream() {
    if (!currentVideoPath) return;
    if (!currentVideoId) {
        addLog("Erreur: ID vidéo manquant", "error");
        return;
    }
    // Prevent double start
    if (isLiveStreaming) {
        addLog('Stream deja en cours', 'info');
        return;
    }


    isLiveStreaming = true;
    const liveStreamImg = document.getElementById('liveStreamImg');
    const loadingOverlay = document.getElementById('streamLoadingOverlay');
    const device = deviceSelect.value;

    // Show loading overlay
    if (loadingOverlay) loadingOverlay.style.display = 'flex';

    liveStreamImg.onload = function() {
        if (loadingOverlay) loadingOverlay.style.display = 'none';
        addLog('Stream actif - premiere frame recue!', 'success');
        liveStreamImg.onload = null; // Important
    };

    // Add error handler to detect stream issues
    liveStreamImg.onerror = () => {
        addLog('Erreur de stream - le modele se charge peut-etre encore...', 'warning');
    };

    // Build stream URL with timestamp to prevent caching
    const streamUrl = `/stream/mjpeg?videoPath=${encodeURIComponent(currentVideoPath)}&videoId=${currentVideoId}&device=${device}&t=${Date.now()}`;
    liveStreamImg.src = streamUrl;

    addLog('Stream MJPEG demarre - chargement du modele IA...', 'info');
}

function stopLiveStream() {
    isLiveStreaming = false;
    const liveStreamImg = document.getElementById('liveStreamImg');
    const liveStreamContainer = document.getElementById('liveStreamContainer');
    const liveStreamTab = document.getElementById('liveStreamTab');
    const loadingOverlay = document.getElementById('streamLoadingOverlay');

    // Clear the stream and handlers
    liveStreamImg.onload = null;
    liveStreamImg.onerror = null;
    liveStreamImg.src = '';
    liveStreamContainer.style.display = 'none';
    videoPlayer.style.display = 'block';

    // Reset loading overlay for next time
    if (loadingOverlay) loadingOverlay.style.display = 'flex';

    // Disable tab after stream ends
    liveStreamTab.disabled = true;

    // Call stop endpoint
    fetch('/stream/stop', { method: 'POST' }).catch(() => {});

    addLog('Live stream arrete', 'info');
}

// ===== Timeline Functions =====
function setupTimeline() {
    if (clearTimelineBtn) {
        clearTimelineBtn.addEventListener('click', clearTimeline);
    }
}

function clearTimeline() {
    if (timelineScroll) {
        timelineScroll.innerHTML = `
            <div class="timeline-empty" id="timelineEmpty">
                <p>En attente de l'analyse...</p>
                <p class="timeline-hint">Les changements de porteur du ballon s'afficheront ici</p>
            </div>
        `;
    }
    lastBallHolderId = null;
    lastTimelineFrame = 0;
    lastBallLostFrame = 0;
}

function frameToTimestamp(frameNum) {
    const totalSeconds = frameNum / videoFps;
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = Math.floor(totalSeconds % 60);
    const ms = Math.floor((totalSeconds % 1) * 100);
    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}.${ms.toString().padStart(2, '0')}`;
}

function updateTimeline(data) {
    const frameNum = data.frame_num ?? data.frameNum ?? 0;
    const ballHolderId = data.ball_holder_id ?? data.ballHolderId;
    const players = data.players ?? [];
    const possession = data.possession;
    const team1NameData = data.team_1_name ?? data.team1Name ?? 'Equipe 1';
    const team2NameData = data.team_2_name ?? data.team2Name ?? 'Equipe 2';

    // Only add event if ball holder changed
    if (ballHolderId && ballHolderId !== lastBallHolderId) {
        // Check if enough frames have passed since last change
        if (frameNum - lastTimelineFrame < MIN_FRAMES_BETWEEN_CHANGES) {
            return; // Too soon, skip this change
        }

        const holder = players.find(p => p.id === ballHolderId);
        if (holder) {
            addTimelineEvent(frameNum, holder, possession, team1NameData, team2NameData);
            lastBallHolderId = ballHolderId;
            lastTimelineFrame = frameNum;
        }
    } else if (!ballHolderId && lastBallHolderId) {
        // Ball lost - but only if enough time has passed
        if (frameNum - lastBallLostFrame < MIN_FRAMES_BETWEEN_BALL_LOST) {
            return; // Too soon for another "ball lost" event
        }
        addTimelineEventBallLost(frameNum, possession);
        lastBallHolderId = null;
        lastBallLostFrame = frameNum;
    }
}

function addTimelineEvent(frameNum, holder, possession, team1Name, team2Name) {
    if (!timelineScroll) return;

    // Remove empty message if present
    const emptyMsg = document.getElementById('timelineEmpty');
    if (emptyMsg) emptyMsg.remove();

    const timestamp = frameToTimestamp(frameNum);
    const jerseyNum = holder.jersey_number ?? holder.jerseyNumber;
    const playerName = holder.player_name ?? holder.playerName;
    const playerId = holder.id;
    const teamId = holder.team;
    const teamName = teamId === 1 ? team1Name : team2Name;
    const teamClass = teamId === 1 ? 'team1' : 'team2';

    // Cap speed to realistic values
    let speed = holder.speed_kmh ?? holder.speedKmh ?? 0;
    if (speed > MAX_REALISTIC_SPEED) {
        speed = 0; // Ignore unrealistic speeds
    }

    // Show jersey number, player name, or tracking ID
    let displayName;
    if (playerName && jerseyNum && jerseyNum <= 99) {
        displayName = `${playerName} #${jerseyNum}`;
    } else if (playerName) {
        displayName = playerName;
    } else if (jerseyNum && jerseyNum <= 99) {
        displayName = `#${jerseyNum}`;
    } else {
        displayName = `Joueur #${playerId}`; // Show tracking ID if no jersey number
    }

    const p1 = possession ? (possession['1'] ?? possession[1] ?? 50) : 50;
    const p2 = possession ? (possession['2'] ?? possession[2] ?? 50) : 50;

    const event = document.createElement('div');
    event.className = `timeline-event ${teamClass}`;
    event.innerHTML = `
        <div class="timeline-time">
            <span class="timeline-timestamp">${timestamp}</span>
        </div>
        <div class="timeline-content">
            <div class="timeline-main">
                <span class="timeline-ball-icon"></span>
                <span class="timeline-player ${teamClass}">${displayName}</span>
            </div>
            <div class="timeline-team-name">${teamName}</div>
            <div class="timeline-stats">
                <span class="timeline-speed">${speed.toFixed(1)} km/h</span>
                <span class="timeline-possession-text">${p1.toFixed(0)}% - ${p2.toFixed(0)}%</span>
            </div>
        </div>
    `;

    // Add at the top (newest first)
    timelineScroll.insertBefore(event, timelineScroll.firstChild);

    // Keep only last 50 events for performance
    while (timelineScroll.children.length > 50) {
        timelineScroll.removeChild(timelineScroll.lastChild);
    }
}

function addTimelineEventBallLost(frameNum, possession) {
    if (!timelineScroll) return;

    // Remove empty message if present
    const emptyMsg = document.getElementById('timelineEmpty');
    if (emptyMsg) emptyMsg.remove();

    const timestamp = frameToTimestamp(frameNum);
    const p1 = possession ? (possession['1'] ?? possession[1] ?? 50) : 50;
    const p2 = possession ? (possession['2'] ?? possession[2] ?? 50) : 50;

    const event = document.createElement('div');
    event.className = 'timeline-event ball-lost';
    event.innerHTML = `
        <div class="timeline-time">
            <span class="timeline-timestamp">${timestamp}</span>
        </div>
        <div class="timeline-content">
            <div class="timeline-main">
                <span class="timeline-ball-icon lost"></span>
                <span class="timeline-lost-text">Ballon perdu</span>
            </div>
            <div class="timeline-stats">
                <span class="timeline-possession-text">${p1.toFixed(0)}% - ${p2.toFixed(0)}%</span>
            </div>
        </div>
    `;

    timelineScroll.insertBefore(event, timelineScroll.firstChild);

    while (timelineScroll.children.length > 50) {
        timelineScroll.removeChild(timelineScroll.lastChild);
    }
}

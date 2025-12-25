package com.savt.backend.application.mapper;

import com.savt.backend.domain.entity.VideoMetadata;
import com.savt.backend.presentation.dto.Response.VideoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
@Mapper(componentModel = "spring", uses = {UserMapper.class, EventMapper.class, CommentMapper.class})
public interface VideoMapper {
    @Mapping(source = "dateUpload", target = "dateUpload", dateFormat = "yyyy-MM-dd HH:mm:ss")
    VideoResponse toResponse(VideoMetadata video);

    List<VideoResponse> toResponseList(List<VideoMetadata> videos);
}

package com.yixian.springbootinit.controller;

import com.yixian.springbootinit.common.Result;
import com.yixian.springbootinit.constant.MessageConstant;
import com.yixian.springbootinit.exception.BaseException;
import com.yixian.springbootinit.model.dto.postthumb.PostThumbAddDTO;
import com.yixian.springbootinit.service.PostThumbService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/post_thumb")
@Slf4j
public class PostThumbController {

    @Resource
    private PostThumbService postThumbService;

    /**
     * 点赞 / 取消点赞
     *
     * @param postThumbAddDTO
     * @return
     */
    @PostMapping("/")
    public Result<Integer> doThumb(@RequestBody PostThumbAddDTO postThumbAddDTO) {
        if (postThumbAddDTO == null || postThumbAddDTO.getPostId() <= 0) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        Long postId = postThumbAddDTO.getPostId();
        int result = postThumbService.doPostThumb(postId);
        return Result.success(result);
    }
}

package com.yixian.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yixian.springbootinit.common.Result;
import com.yixian.springbootinit.constant.MessageConstant;
import com.yixian.springbootinit.context.BaseContext;
import com.yixian.springbootinit.exception.BaseException;
import com.yixian.springbootinit.model.dto.post.PostQueryDTO;
import com.yixian.springbootinit.model.dto.postfavour.PostFavourAddDTO;
import com.yixian.springbootinit.model.entity.Post;
import com.yixian.springbootinit.model.vo.PostVO;
import com.yixian.springbootinit.service.PostFavourService;
import com.yixian.springbootinit.service.PostService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/post_favour")
@Slf4j
public class PostFavourController {
    @Resource
    private PostFavourService postFavourService;

    @Resource
    private PostService postService;

    /**
     * 收藏 / 取消收藏
     *
     * @param postFavourAddDTO
     * @return
     */
    @PostMapping("/")
    public Result<Integer> doPostFavour(@RequestBody PostFavourAddDTO postFavourAddDTO) {
        if (postFavourAddDTO == null || postFavourAddDTO.getPostId() <= 0) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        Long postId = postFavourAddDTO.getPostId();
        int result = postFavourService.doPostFavour(postId);
        return Result.success(result);
    }

    /**
     * 获取我收藏的帖子列表
     *
     * @param postQueryDTO
     * @return
     */
    @PostMapping("/my/list/page")
    public Result<Page<PostVO>> listMyFavourPostByPage(@RequestBody PostQueryDTO postQueryDTO) {
        if (postQueryDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        int current = postQueryDTO.getCurrent();
        int size = postQueryDTO.getPageSize();
        // 限制爬虫
        if (size > 20) {
            throw new BaseException(MessageConstant.PARAMS_ERROR);
        }
        Page<Post> postPage = postFavourService.listFavourPostByPage(new Page<>(current, size),
                postService.getQueryWrapper(postQueryDTO), BaseContext.getCurrentId());
        return Result.success(postService.getPostVOPage(postPage));
    }
}

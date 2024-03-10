package com.yixian.springbootinit.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yixian.springbootinit.common.DeleteRequest;
import com.yixian.springbootinit.common.Result;
import com.yixian.springbootinit.constant.MessageConstant;
import com.yixian.springbootinit.context.BaseContext;
import com.yixian.springbootinit.exception.BaseException;
import com.yixian.springbootinit.model.dto.post.PostAddDTO;
import com.yixian.springbootinit.model.dto.post.PostEditDTO;
import com.yixian.springbootinit.model.dto.post.PostQueryDTO;
import com.yixian.springbootinit.model.dto.post.PostUpdateDTO;
import com.yixian.springbootinit.model.entity.Post;
import com.yixian.springbootinit.model.entity.User;
import com.yixian.springbootinit.model.vo.PostVO;
import com.yixian.springbootinit.service.PostService;
import com.yixian.springbootinit.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.events.MappingEndEvent;

import java.util.List;

@RestController
@RequestMapping("/post")
@Slf4j
public class PostController {

    @Resource
    private PostService postService;

    @Resource
    private UserService userService;


    // region 增删改查

    /**
     * 新增帖子
     *
     * @param postAddDTO
     * @return
     */
    @PostMapping("add")
    public Result<Long> addPost(@RequestBody PostAddDTO postAddDTO) {
        if (postAddDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        Post post = new Post();
        BeanUtils.copyProperties(postAddDTO, post);
        List<String> tags = postAddDTO.getTags();
        if (tags != null) {
            post.setTags(JSONUtil.toJsonStr(tags));
        }
        postService.validPost(post, true);
        post.setUserId(BaseContext.getCurrentId());
        post.setFavourNum(0);
        post.setThumbNum(0);
        boolean result = postService.save(post);
        if (!result) {
            throw new BaseException(MessageConstant.OPERATION_ERROR);
        }
        return Result.success(post.getId());
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    public Result<Boolean> deletePost(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        Long id = deleteRequest.getId();
        // 判断是否存在
        Post oldPost = postService.getById(id);
        if (oldPost == null) {
            throw new BaseException(MessageConstant.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldPost.getUserId().equals(BaseContext.getCurrentId()) && userService.isAdmin(BaseContext.getCurrentId())) {
            throw new BaseException(MessageConstant.NO_AUTH_ERROR);
        }
        boolean result = postService.removeById(id);
        return Result.success(result);
    }

    /**
     * 更新
     *
     * @param postUpdateDTO
     * @return
     */
    @PostMapping("/update")
    public Result<Boolean> updatePost(@RequestBody PostUpdateDTO postUpdateDTO) {
        if (postUpdateDTO == null || postUpdateDTO.getId() <= 0) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        Post post = new Post();
        BeanUtils.copyProperties(postUpdateDTO, post);
        List<String> tags = postUpdateDTO.getTags();
        if (tags != null) {
            post.setTags(JSONUtil.toJsonStr(tags));
        }
        // 参数校验
        postService.validPost(post, false);
        Long id = postUpdateDTO.getId();
        // 判断是否存在
        Post oldPost = postService.getById(id);
        if (oldPost == null) {
            throw new BaseException(MessageConstant.NOT_FOUND_ERROR);
        }
        boolean result = postService.updateById(post);
        return Result.success(result);
    }


    /**
     * 根据 id 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public Result<PostVO> getPostVOById(Long id) {
        if (id <= 0) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        Post post = postService.getById(id);
        if (post == null) {
            throw new BaseException(MessageConstant.NOT_FOUND_ERROR);
        }
        return Result.success(postService.getPostVO(post));
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param postQueryDTO
     * @return
     */
    @PostMapping("/list/page")
    public Result<Page<Post>> listPostByPage(@RequestBody PostQueryDTO postQueryDTO) {
        long current = postQueryDTO.getCurrent();
        long size = postQueryDTO.getPageSize();
        Page<Post> postPage = postService.page(new Page<>(current, size),
                postService.getQueryWrapper(postQueryDTO));
        return Result.success(postPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param postQueryDTO
     * @return
     */
    @PostMapping("/list/page/vo")
    public Result<Page<PostVO>> listPostVOByPage(@RequestBody PostQueryDTO postQueryDTO) {
        long current = postQueryDTO.getCurrent();
        long size = postQueryDTO.getPageSize();
        // 限制爬虫
        if (size > 20) {
            throw new BaseException(MessageConstant.PARAMS_ERROR);
        }
        Page<Post> postPage = postService.page(new Page<>(current, size),
                postService.getQueryWrapper(postQueryDTO));
        return Result.success(postService.getPostVOPage(postPage));
    }


    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param postQueryDTO
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public Result<Page<PostVO>> listMyPostVOByPage(@RequestBody PostQueryDTO postQueryDTO) {
        if (postQueryDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        postQueryDTO.setUserId(BaseContext.getCurrentId());
        long current = postQueryDTO.getCurrent();
        long size = postQueryDTO.getPageSize();
        // 限制爬虫
        if (size > 20) {
            throw new BaseException(MessageConstant.PARAMS_ERROR);
        }
        Page<Post> postPage = postService.page(new Page<>(current, size),
                postService.getQueryWrapper(postQueryDTO));
        return Result.success(postService.getPostVOPage(postPage));
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param postEditDTO
     * @return
     */
    @PostMapping("/edit")
    public Result<Boolean> editPost(@RequestBody PostEditDTO postEditDTO) {
        if (postEditDTO == null || postEditDTO.getId() <= 0) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        Post post = new Post();
        BeanUtils.copyProperties(postEditDTO, post);
        List<String> tags = postEditDTO.getTags();
        if (tags != null) {
            post.setTags(JSONUtil.toJsonStr(tags));
        }
        // 参数校验
        postService.validPost(post, false);
        long id = postEditDTO.getId();
        // 判断是否存在
        Post oldPost = postService.getById(id);
        if (oldPost == null) {
            throw new BaseException(MessageConstant.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可编辑
        if (!oldPost.getUserId().equals(BaseContext.getCurrentId()) && !userService.isAdmin(BaseContext.getCurrentId())) {
            throw new BaseException(MessageConstant.NO_AUTH_ERROR);
        }
        boolean result = postService.updateById(post);
        return Result.success(result);
    }
}

package com.yixian.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yixian.springbootinit.model.dto.post.PostQueryDTO;
import com.yixian.springbootinit.model.entity.Post;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yixian.springbootinit.model.vo.PostVO;

/**
 * @author jiangfei
 * @description 针对表【post(帖子)】的数据库操作Service
 * @createDate 2024-03-10 13:33:14
 */
public interface PostService extends IService<Post> {

    /**
     * 校验
     *
     * @param post
     * @param add
     */
    void validPost(Post post, boolean add);

    /**
     * 获取帖子封装
     *
     * @param post
     * @return
     */
    PostVO getPostVO(Post post);

    /**
     * 获取查询条件
     *
     * @param postQueryDTO
     * @return
     */
    Wrapper<Post> getQueryWrapper(PostQueryDTO postQueryDTO);

    /**
     * 分页获取帖子封装
     *
     * @param postPage
     * @return
     */
    Page<PostVO> getPostVOPage(Page<Post> postPage);
}

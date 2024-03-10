package com.yixian.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yixian.springbootinit.model.entity.Post;
import com.yixian.springbootinit.model.entity.PostFavour;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yixian.springbootinit.model.entity.PostThumb;

/**
 * @author jiangfei
 * @description 针对表【post_favour(帖子收藏)】的数据库操作Service
 * @createDate 2024-03-10 15:26:46
 */
public interface PostFavourService extends IService<PostFavour> {

    /**
     * 帖子收藏
     *
     * @param postId
     * @return
     */
    int doPostFavour(Long postId);

    /**
     * 帖子收藏（内部服务）
     *
     * @param userId
     * @param postId
     * @return
     */
    int doPostFavourInner(Long userId, Long postId);

    /**
     * 分页获取用户收藏的帖子列表
     *
     * @param page
     * @param queryWrapper
     * @param favourUserId
     * @return
     */
    Page<Post> listFavourPostByPage(IPage<Post> page, Wrapper<Post> queryWrapper, Long favourUserId);
}

package com.yixian.springbootinit.service;

import com.yixian.springbootinit.model.entity.PostThumb;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jiangfei
 * @description 针对表【post_thumb(帖子点赞)】的数据库操作Service
 * @createDate 2024-03-10 15:26:50
 */
public interface PostThumbService extends IService<PostThumb> {

    /**
     * 点赞
     *
     * @param postId
     * @return
     */
    int doPostThumb(Long postId);

    /**
     * 帖子点赞 （内部服务）
     *
     * @param userId
     * @param postId
     * @return
     */
    int doPostThumbInner(Long userId, Long postId);
}

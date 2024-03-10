package com.yixian.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yixian.springbootinit.constant.MessageConstant;
import com.yixian.springbootinit.context.BaseContext;
import com.yixian.springbootinit.exception.BaseException;
import com.yixian.springbootinit.model.entity.Post;
import com.yixian.springbootinit.model.entity.PostThumb;
import com.yixian.springbootinit.service.PostService;
import com.yixian.springbootinit.service.PostThumbService;
import com.yixian.springbootinit.mapper.PostThumbMapper;
import jakarta.annotation.Resource;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jiangfei
 * @description 针对表【post_thumb(帖子点赞)】的数据库操作Service实现
 * @createDate 2024-03-10 15:26:50
 */
@Service
public class PostThumbServiceImpl extends ServiceImpl<PostThumbMapper, PostThumb>
        implements PostThumbService {

    @Resource
    private PostService postService;

    /**
     * 点赞
     *
     * @param postId
     * @return
     */
    @Override
    public int doPostThumb(Long postId) {
        // 判断实体是否存在，根据类别获得实体
        Post post = postService.getById(postId);
        if (post == null) {
            throw new BaseException(MessageConstant.NOT_FOUND_ERROR);
        }
        // 是否已点赞
        Long userId = BaseContext.getCurrentId();
        // 每个用户串行点赞
        // 锁必须要包裹住事务方法
        PostThumbService postThumbService = (PostThumbService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            return postThumbService.doPostThumbInner(userId, postId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doPostThumbInner(Long userId, Long postId) {
        PostThumb postThumb = new PostThumb();
        postThumb.setUserId(userId);
        postThumb.setPostId(postId);
        QueryWrapper<PostThumb> thumbQueryWrapper = new QueryWrapper<>(postThumb);
        PostThumb oldPostThumb = this.getOne(thumbQueryWrapper);
        boolean result;
        // 已点赞
        if (oldPostThumb != null) {
            result = this.remove(thumbQueryWrapper);
            if (result) {
                // 点赞数 -1
                result = postService.update()
                        .eq("id", postId)
                        .gt("thumbNum", 0)
                        .setSql("thumbNum=thumbNum-1")
                        .update();
                return result ? -1 : 0;
            } else {
                throw new BaseException(MessageConstant.SYSTEM_ERROR);
            }
        } else {
            // 未点赞
            result = this.save(postThumb);
            if (result) {
                // 点赞数 +1
                result = postService.update()
                        .eq("id", postId)
                        .setSql("thumbNum = thumbNum+1")
                        .update();
                return result ? 1 : 0;
            } else {
                throw new BaseException(MessageConstant.SYSTEM_ERROR);
            }
        }
    }
}





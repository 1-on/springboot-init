package com.yixian.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yixian.springbootinit.constant.MessageConstant;
import com.yixian.springbootinit.context.BaseContext;
import com.yixian.springbootinit.exception.BaseException;
import com.yixian.springbootinit.model.entity.Post;
import com.yixian.springbootinit.model.entity.PostFavour;
import com.yixian.springbootinit.model.entity.PostThumb;
import com.yixian.springbootinit.service.PostFavourService;
import com.yixian.springbootinit.mapper.PostFavourMapper;
import com.yixian.springbootinit.service.PostService;
import jakarta.annotation.Resource;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jiangfei
 * @description 针对表【post_favour(帖子收藏)】的数据库操作Service实现
 * @createDate 2024-03-10 15:26:46
 */
@Service
public class PostFavourServiceImpl extends ServiceImpl<PostFavourMapper, PostFavour>
        implements PostFavourService {

    @Resource
    private PostService postService;

    @Override
    public int doPostFavour(Long postId) {
        // 判断是否存在
        Post post = postService.getById(postId);
        if (post == null) {
            throw new BaseException(MessageConstant.NOT_FOUND_ERROR);
        }
        // 是否已收藏帖子
        Long userId = BaseContext.getCurrentId();
        // 每个用户串行帖子收藏
        // 锁必须要包裹住事务方法
        PostFavourService postFavourService = (PostFavourService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            return postFavourService.doPostFavourInner(userId, postId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doPostFavourInner(Long userId, Long postId) {
        PostFavour postFavour = new PostFavour();
        postFavour.setUserId(userId);
        postFavour.setPostId(postId);
        QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>(postFavour);
        PostFavour oldPostFavour = this.getOne(postFavourQueryWrapper);
        boolean result;
        // 已收藏
        if (oldPostFavour != null) {
            result = this.remove(postFavourQueryWrapper);
            if (result) {
                // 点赞数 -1
                result = postService.update()
                        .eq("id", postId)
                        .gt("favourNum", 0)
                        .setSql("favourNum=favourNum-1")
                        .update();
                return result ? -1 : 0;
            } else {
                throw new BaseException(MessageConstant.SYSTEM_ERROR);
            }
        } else {
            // 未收藏
            result = this.save(postFavour);
            if (result) {
                // 点赞数 +1
                result = postService.update()
                        .eq("id", postId)
                        .setSql("favourNum = favourNum+1")
                        .update();
                return result ? 1 : 0;
            } else {
                throw new BaseException(MessageConstant.SYSTEM_ERROR);
            }
        }
    }

    @Override
    public Page<Post> listFavourPostByPage(IPage<Post> page, Wrapper<Post> queryWrapper, Long favourUserId) {
        if (favourUserId <= 0) {
            return new Page<>();
        }
        return baseMapper.listFavourPostByPage(page, queryWrapper, favourUserId);
    }
}





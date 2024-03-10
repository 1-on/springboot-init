package com.yixian.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yixian.springbootinit.constant.CommonConstant;
import com.yixian.springbootinit.constant.MessageConstant;
import com.yixian.springbootinit.context.BaseContext;
import com.yixian.springbootinit.exception.BaseException;
import com.yixian.springbootinit.mapper.PostFavourMapper;
import com.yixian.springbootinit.mapper.PostMapper;
import com.yixian.springbootinit.mapper.PostThumbMapper;
import com.yixian.springbootinit.model.dto.post.PostQueryDTO;
import com.yixian.springbootinit.model.entity.Post;
import com.yixian.springbootinit.model.entity.PostFavour;
import com.yixian.springbootinit.model.entity.PostThumb;
import com.yixian.springbootinit.model.entity.User;
import com.yixian.springbootinit.model.vo.PostVO;
import com.yixian.springbootinit.model.vo.UserVO;
import com.yixian.springbootinit.service.PostService;
import com.yixian.springbootinit.service.UserService;
import com.yixian.springbootinit.utils.SqlUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jiangfei
 * @description 针对表【post(帖子)】的数据库操作Service实现
 * @createDate 2024-03-10 13:33:14
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
        implements PostService {

    @Resource
    private UserService userService;

    @Resource
    private PostThumbMapper postThumbMapper;

    @Resource
    private PostFavourMapper postFavourMapper;

    @Override
    public void validPost(Post post, boolean add) {
        if (post == null) {
            throw new BaseException(MessageConstant.PARAMS_ERROR);
        }
        String title = post.getTitle();
        String content = post.getContent();
        String tags = post.getTags();
        // 创建时，参数不能为空
        if (add) {
            if (StringUtils.isAnyBlank(title, content, tags)) {
                throw new BaseException(MessageConstant.PARAMS_ERROR);
            }
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BaseException(MessageConstant.TITLE_TOO_LONG);
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BaseException(MessageConstant.CONTENT_TOO_LONG);
        }
    }

    @Override
    public PostVO getPostVO(Post post) {
        Long postId = post.getId();
        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post, postVO);
        postVO.setTagList(JSONUtil.toList(post.getTags(), String.class));
        // 1.关联查询用户信息
        Long userId = post.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        postVO.setUser(userVO);
        // 2.已登录，获取用户点赞、收藏状态
        Long loginUserId = BaseContext.getCurrentId();
        if (loginUserId != null) {
            // 获取点赞
            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
            postThumbQueryWrapper.eq("postId", postId);
            postThumbQueryWrapper.eq("userId", loginUserId);
            PostThumb postThumb = postThumbMapper.selectOne(postThumbQueryWrapper);
            postVO.setHasThumb(postThumb != null);
            // 获取收藏
            QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
            postFavourQueryWrapper.eq("postId", postId);
            postFavourQueryWrapper.eq("userId", loginUserId);
            PostFavour postFavour = postFavourMapper.selectOne(postFavourQueryWrapper);
            postVO.setHasFavour(postFavour != null);
        }
        return postVO;
    }

    @Override
    public Wrapper<Post> getQueryWrapper(PostQueryDTO postQueryDTO) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        if (postQueryDTO == null) {
            return queryWrapper;
        }
        Long id = postQueryDTO.getId();
        Long notId = postQueryDTO.getNotId();
        String searchText = postQueryDTO.getSearchText();
        String title = postQueryDTO.getTitle();
        String content = postQueryDTO.getContent();
        List<String> tagList = postQueryDTO.getTags();
        List<String> orTags = postQueryDTO.getOrTags();
        Long userId = postQueryDTO.getUserId();
        Long favourUserId = postQueryDTO.getFavourUserId();
        int current = postQueryDTO.getCurrent();
        int pageSize = postQueryDTO.getPageSize();
        String sortField = postQueryDTO.getSortField();
        String sortOrder = postQueryDTO.getSortOrder();
        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    @Override
    public Page<PostVO> getPostVOPage(Page<Post> postPage) {
        List<Post> postList = postPage.getRecords();
        Page<PostVO> postVOPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        if (CollUtil.isEmpty(postList)) {
            return postVOPage;
        }
        // 1.关联查询用户信息
        Set<Long> userIdSet = postList.stream().map(Post::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        // 2.已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> postIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> postIdHasFavourMap = new HashMap<>();
        Long loginUserId = BaseContext.getCurrentId();
        if (loginUserId != null) {
            Set<Long> postIdSet = postList.stream().map(Post::getId).collect(Collectors.toSet());
            // 获取点赞
            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
            postThumbQueryWrapper.in("postId", postIdSet);
            postThumbQueryWrapper.eq("userId", loginUserId);
            List<PostThumb> postThumbList = postThumbMapper.selectList(postThumbQueryWrapper);
            postThumbList.forEach(postThumb -> postIdHasThumbMap.put(postThumb.getPostId(), true));
            // 获取收藏
            QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
            postFavourQueryWrapper.in("postId", postIdSet);
            postThumbQueryWrapper.eq("userId", loginUserId);
            List<PostFavour> postFavourList = postFavourMapper.selectList(postFavourQueryWrapper);
            postFavourList.forEach(postFavour -> postIdHasFavourMap.put(postFavour.getPostId(), true));
        }
        // 填充信息

        List<PostVO> postVOList = postList.stream().map(post -> {
            PostVO postVO = new PostVO();
            BeanUtils.copyProperties(post, postVO);
            postVO.setTagList(JSONUtil.toList(post.getTags(), String.class));
            Long userId = post.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            postVO.setUser(userService.getUserVO(user));
            postVO.setHasThumb(postIdHasThumbMap.getOrDefault(post.getId(), false));
            postVO.setHasFavour(postIdHasFavourMap.getOrDefault(post.getId(), false));
            return postVO;
        }).collect(Collectors.toList());
        postVOPage.setRecords(postVOList);
        return postVOPage;
    }
}





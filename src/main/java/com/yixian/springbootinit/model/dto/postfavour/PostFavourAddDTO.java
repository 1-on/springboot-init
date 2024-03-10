package com.yixian.springbootinit.model.dto.postfavour;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 帖子收藏
 * @TableName post_favour
 */
@Data
public class PostFavourAddDTO implements Serializable {


    /**
     * 帖子 id
     */
    private Long postId;


    private static final long serialVersionUID = 1L;
}
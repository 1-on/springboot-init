package com.yixian.springbootinit.model.dto.postthumb;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class PostThumbAddDTO implements Serializable {

    /**
     * 帖子 id
     */
    private Long postId;


    private static final long serialVersionUID = 1L;
}

package com.lsj.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lsj.ai.domain.po.AiMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiMessageMapper extends BaseMapper<AiMessage> {
}
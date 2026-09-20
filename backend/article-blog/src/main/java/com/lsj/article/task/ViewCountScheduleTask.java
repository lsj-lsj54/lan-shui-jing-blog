package com.lsj.article.task;

import com.lsj.article.service.IArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 浏览量定时落库任务
 * 定期将 Redis 中的浏览量计数器批量写入数据库，避免每次访问都写数据库
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountScheduleTask {

    private final IArticleService articleService;

    /**
     * 每5分钟执行一次浏览量落库
     * cron: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void flushViewCount() {
        try {
            articleService.flushViewCountToDB();
        } catch (Exception e) {
            log.error("浏览量定时落库任务执行失败", e);
        }
    }
}

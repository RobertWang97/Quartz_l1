package com.hello.jobs;

import com.hello.entities.JobInfo;
import org.quartz.*;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.LocalDateTime;

/**
 * @author Y
 * @DisallowConcurrentExecution：这个注解的作用就是同一个任务必须在上一次执行完毕之后，再按照corn时间执行，不会并行执行
 * @PersistJobDataAfterExecution：这个注解的作用就是下一个任务用到上一个任务的修改数据（定时任务里面的jobData数据流转）
 * @description 任务1    这两个注解作用是
 * @date 2023/6/28
 */
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class JobOne extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext context) {
        System.out.println("TimeEventJob正在执行..." + LocalDateTime.now());
        // 执行10秒
        try {
//            Thread.sleep(9000);
            System.out.println("*************TimeEventJob正在执行************" + LocalDateTime.now());
            JobKey jobKey = JobKey.jobKey("job1", "group1");
            Scheduler scheduler = context.getScheduler();
            JobKey curJobKey = context.getJobDetail().getKey();
            if (scheduler.checkExists(jobKey) && jobKey.equals(curJobKey)) {
                System.out.println("Equales job1, groups, delete job!");

                // 若Job存在，则删除该Job
                scheduler.deleteJob(jobKey);

                System.out.println("成功删除Job！");

            }
            System.out.println("TimeEventJob执行完毕..." + LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

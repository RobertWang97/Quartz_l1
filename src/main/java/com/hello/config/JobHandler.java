package com.hello.config;

import com.hello.entities.JobInfo;
import org.quartz.*;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Configuration
public class JobHandler {

    @Resource
    private Scheduler scheduler;

    /**
     * 添加任务
     */
    @SuppressWarnings("unchecked")
    public void addJob(JobInfo jobInfo) throws SchedulerException, ClassNotFoundException {
        Objects.requireNonNull(jobInfo, "任务信息不能为空");

        // 生成job key
        JobKey jobKey = JobKey.jobKey(jobInfo.getJobName(), jobInfo.getJobGroup());
        // 当前任务不存在才进行添加
        if (!scheduler.checkExists(jobKey)) {
            Class<Job> jobClass = (Class<Job>)Class.forName(jobInfo.getClassName());
            // 任务明细
            JobDetail jobDetail = JobBuilder
                    .newJob(jobClass)
                    .withIdentity(jobKey)
                    .withIdentity(jobInfo.getJobName(), jobInfo.getJobGroup())
                    .withDescription(jobInfo.getJobName())
                    .build();
            // 配置信息
            jobDetail.getJobDataMap().put("config", jobInfo.getConfig());
            // 定义触发器
            TriggerKey triggerKey = TriggerKey.triggerKey(jobInfo.getTriggerName(), jobInfo.getTriggerGroup());
            // 设置任务的错过机制
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .startNow()
//                    .withSchedule(CronScheduleBuilder.cronSchedule(jobInfo.getCron()).withMisfireHandlingInstructionDoNothing())
                    .withSchedule(CronScheduleBuilder.cronSchedule("*/10 * * * * ?"))
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
        } else {
            throw new SchedulerException(jobInfo.getJobName() + "任务已存在，无需重复添加");
        }
    }

    /**
     * 任务暂停
     */
    public void pauseJob(String jobGroup, String jobName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        if (scheduler.checkExists(jobKey)) {
            scheduler.pauseJob(jobKey);
        }
    }

    /**
     * 继续任务
     */
    public void continueJob(String jobGroup, String jobName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        if (scheduler.checkExists(jobKey)) {
            scheduler.resumeJob(jobKey);
        }
    }

    /**
     * 删除任务
     */
    public boolean deleteJob(String jobGroup, String jobName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        if (scheduler.checkExists(jobKey)) {
            // 这里还需要先删除trigger相关
            //TriggerKey triggerKey = TriggerKey.triggerKey(jobInfo.getTriggerName(), jobInfo.getTriggerGroup());
            //scheduler.getTrigger()
            //scheduler.rescheduleJob()
            return scheduler.deleteJob(jobKey);
        }
        return false;
    }

    /**
     * 获取任务信息
     */
    public JobInfo getJobInfo(String jobGroup, String jobName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        if (!scheduler.checkExists(jobKey)) {
            return null;
        }
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
        if (Objects.isNull(triggers)) {
            throw new SchedulerException("未获取到触发器信息");
        }
        TriggerKey triggerKey = triggers.get(0).getKey();
        Trigger.TriggerState triggerState = scheduler.getTriggerState(triggerKey);
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);

        JobInfo jobInfo = new JobInfo();
        jobInfo.setJobName(jobName);
        jobInfo.setJobGroup(jobGroup);
        jobInfo.setTriggerName(triggerKey.getName());
        jobInfo.setTriggerGroup(triggerKey.getGroup());
        jobInfo.setClassName(jobDetail.getJobClass().getName());
        jobInfo.setStatus(triggerState.toString());

        if (Objects.nonNull(jobDetail.getJobDataMap())) {
//            jobInfo.setConfig(JSONObject.toJSONString(jobDetail.getJobDataMap()));
            jobInfo.setConfig("xxxxxxxxxxxxxx");

        }

        CronTrigger theTrigger = (CronTrigger) triggers.get(0);
        jobInfo.setCron(theTrigger.getCronExpression());
        return jobInfo;
    }
}

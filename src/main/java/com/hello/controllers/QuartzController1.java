package com.hello.controllers;

import com.hello.config.JobHandler;
import com.hello.entities.JobInfo;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/job")
public class QuartzController1 {

    @Resource
    private JobHandler jobHandler;
    @Resource
    private Scheduler scheduler;

    /**
     * 查询所有的任务
     */
    @RequestMapping("/all")
    @ResponseBody
    public List<JobInfo> list() throws SchedulerException {
        List<JobInfo> jobInfos = new ArrayList<>();
        List<String> triggerGroupNames = scheduler.getTriggerGroupNames();
        for (String triggerGroupName : triggerGroupNames) {
            Set<TriggerKey> triggerKeySet = scheduler
                    .getTriggerKeys(GroupMatcher.triggerGroupEquals(triggerGroupName));
            for (TriggerKey triggerKey : triggerKeySet) {
                Trigger trigger = scheduler.getTrigger(triggerKey);
                JobKey jobKey = trigger.getJobKey();
                JobInfo jobInfo = jobHandler.getJobInfo(jobKey.getGroup(), jobKey.getName());
                jobInfos.add(jobInfo);
            }
        }
        return jobInfos;
    }

    /**
     * 添加任务
     */
    @PostMapping("/add")
    @ResponseBody
    public JobInfo addJob(@RequestBody JobInfo jobInfo) throws SchedulerException, ClassNotFoundException {
        jobHandler.addJob(jobInfo);
        return jobInfo;
    }

    /**
     * 暂停任务
     */
    @RequestMapping("/pause")
    public void pauseJob(@RequestParam("jobGroup") String jobGroup, @RequestParam("jobName") String jobName)
            throws SchedulerException {
        jobHandler.pauseJob(jobGroup, jobName);
    }

    /**
     * 继续任务
     */
    @RequestMapping("/continue")
    public void continueJob(@RequestParam("jobGroup") String jobGroup, @RequestParam("jobName") String jobName)
            throws SchedulerException {
        jobHandler.continueJob(jobGroup, jobName);
    }

    /**
     * 删除任务
     */
    @RequestMapping("/delete")
    @ResponseBody
    public boolean deleteJob(@RequestParam("jobGroup") String jobGroup, @RequestParam("jobName") String jobName)
            throws SchedulerException {
        return jobHandler.deleteJob(jobGroup, jobName);
    }
}

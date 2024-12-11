package com.discord.neocoin;

import java.util.Date;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class MonthlyJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println("一か月に一回の処理を実行します。現在の時刻: " + new Date());
		// 実行する処理をここに記述します。
	}

	public void monthJob(String[] args) {
		try {
			// スケジューラの作成
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

			// ジョブの作成
			JobDetail job = JobBuilder.newJob(MonthlyJob.class)
					.withIdentity("monthlyJob")
					.build();

			// トリガーの作成 (毎月1日の午前0時に実行する)
			Trigger trigger = TriggerBuilder.newTrigger()
					.withIdentity("monthlyTrigger")
					.withSchedule(CronScheduleBuilder.monthlyOnDayAndHourAndMinute(1, 0, 0))
					.build();

			// スケジューラにジョブとトリガーを設定
			scheduler.scheduleJob(job, trigger);

			// スケジューラを開始
			scheduler.start();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
}

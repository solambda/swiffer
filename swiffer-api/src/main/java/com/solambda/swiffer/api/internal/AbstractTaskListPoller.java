package com.solambda.swiffer.api.internal;

import com.solambda.swiffer.api.TaskListPoller;

public class AbstractTaskListPoller implements TaskListPoller {

	private String domainName;
	private String taskList;

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getTaskList() {
		return taskList;
	}

}

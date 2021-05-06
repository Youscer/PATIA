package fr.utils;

import java.util.HashSet;
import java.util.Set;

public abstract class Logs {
	private static Set<LogType> logtypes = new HashSet<LogType>();
	
	public static void init(Set<LogType> logtypes) {
		Logs.logtypes = logtypes;
	}
	
	public static void log(LogType type, String log) {
		if(logtypes.contains(type)) {
			System.out.println(log);
		}
	}
}

/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.misc;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.OptionHandler;

/**
 * A minimal logging layout displaying the elapsed time in seconds since
 * application start, followed by a priority indicator and the message itself.
 * 
 */
public class TimeLayout extends Layout implements OptionHandler {

	public String format(LoggingEvent e) {
		char l = '-';
		switch (e.getLevel().toInt()) {
		case Level.TRACE_INT:
			l = 'T';
			break;
		case Level.DEBUG_INT:
			l = 'D';
			break;
		case Level.ERROR_INT:
			l = 'E';
			break;
		case Level.WARN_INT:
			l = 'W';
			break;
		}
		long ms = e.timeStamp - LoggingEvent.getStartTime();
		return String.format("%07d.%03d " + l + " " + e.getRenderedMessage() + "\n", ms / 1000, ms % 1000);
	}

	public boolean ignoresThrowable() {
		return true;
	}

	public void activateOptions() {
	}
}

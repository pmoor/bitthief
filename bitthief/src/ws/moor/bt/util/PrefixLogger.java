/*
 * BitThief - A Free Riding BitTorrent Client
 * Copyright (C) 2006 Patrick Moor <patrick@moor.ws>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 */

package ws.moor.bt.util;

import org.apache.log4j.Logger;

/**
 * TODO(pmoor): Javadoc
 */
public class PrefixLogger {

  private final Logger logger;
  private final String prefix;

  public PrefixLogger(Logger logger, String prefix) {
    this.logger = logger;
    this.prefix = prefix;
  }

  private Object translateMessage(Object message) {
    return prefix + message;
  }

  public void trace(Object message) {
    logger.trace(translateMessage(message));
  }

  public void trace(Object message, Throwable t) {
    logger.trace(translateMessage(message), t);
  }

  public void debug(Object message) {
    logger.debug(translateMessage(message));
  }

  public void debug(Object message, Throwable t) {
    logger.debug(translateMessage(message), t);
  }

  public void error(Object message) {
    logger.error(translateMessage(message));
  }

  public void error(Object message, Throwable t) {
    logger.error(translateMessage(message), t);
  }

  public void fatal(Object message) {
    logger.fatal(translateMessage(message));
  }

  public void fatal(Object message, Throwable t) {
    logger.fatal(translateMessage(message), t);
  }

  public void info(Object message) {
    logger.info(translateMessage(message));
  }

  public void info(Object message, Throwable t) {
    logger.info(translateMessage(message), t);
  }

  public void warn(Object message) {
    logger.warn(translateMessage(message));
  }

  public void warn(Object message, Throwable t) {
    logger.warn(translateMessage(message), t);
  }
}

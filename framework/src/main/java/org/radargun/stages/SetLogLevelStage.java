/* 
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.radargun.stages;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.radargun.DistStageAck;

public class SetLogLevelStage extends AbstractDistStage {

   private String pkg;
   private String priority;
   
   @Override
   public DistStageAck executeOnSlave() {
      try {
         Logger.getLogger(pkg).setLevel(Level.toLevel(priority));
      } catch (Exception e) {
         log.error("Failed to change log level", e);
      }
      return newDefaultStageAck();
   }

   public void setPackage(String pkg) {
      this.pkg = pkg;
   }

   public void setPriority(String priority) {
      this.priority = priority;
   }
   
   @Override
   public String toString() {
	   return "SetLogLevel(package=" + pkg + ", priority=" + Level.toLevel(priority).toString() + ", " + super.toString();
   }
}
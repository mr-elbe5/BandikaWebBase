/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.rights;

import java.util.*;

public enum SystemZone {
    APPLICATION, USER, CONTENTREAD,
    CONTENTEDIT, CONTENTAPPROVE, CONTENTADMINISTRATION;

    private static List<SystemZone> elevatedZones = Arrays.asList(APPLICATION, USER, CONTENTREAD, CONTENTEDIT, CONTENTAPPROVE, CONTENTADMINISTRATION);
    private static List<SystemZone> contentReadZones = Arrays.asList(CONTENTREAD, CONTENTEDIT, CONTENTAPPROVE, CONTENTADMINISTRATION);
    private static List<SystemZone> contentEditZones = Arrays.asList(CONTENTEDIT, CONTENTAPPROVE, CONTENTADMINISTRATION);
    private static List<SystemZone> contentApproveZones = Arrays.asList(CONTENTAPPROVE, CONTENTADMINISTRATION);
    private static List<SystemZone> contentAdminZones = List.of(CONTENTADMINISTRATION);

    public static boolean includesElevatedZone(Set<SystemZone> zones){
        return includesAnyZoneOf(zones, elevatedZones);
    }

    public static boolean includesContentReadZone(Set<SystemZone> zones){
        return includesAnyZoneOf(zones, contentReadZones);
    }

    public static boolean includesContentEditZone(Set<SystemZone> zones){
        return includesAnyZoneOf(zones, contentEditZones);
    }

    public static boolean includesContentApproveZone(Set<SystemZone> zones){
        return includesAnyZoneOf(zones, contentApproveZones);
    }

    public static boolean includesContentAdminZone(Set<SystemZone> zones){
        return includesAnyZoneOf(zones, contentAdminZones);
    }

    private static boolean includesAnyZoneOf(Set<SystemZone> zones, List<SystemZone> validZones){
        for (SystemZone zone : validZones){
            if (zones.contains(zone)){
                return true;
            }
        }
        return false;
    }
}



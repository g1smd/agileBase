/*
 *  Copyright 2010 GT webMarque Ltd
 * 
 *  This file is part of agileBase.
 *
 *  agileBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  agileBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with agileBase.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gtwm.pb.model.manageSchema;

/**
 * INNER, LEFT_OUTER, RIGHT_OUTER and FULL_OUTER are the standard SQL join types.
 * 
 * NONE means don't do 'table 1 join table 2 on condition', but just 'table 1, table 2'. This has the effect
 * of including data in each row of table 2 for every row of table 1. e.g. if table 1 has 10 rows and table 2
 * 2, the join will result in 20 rows. This type of join is useful to use when table 2 has only one row. You
 * can include constants in calculations without having to hard code them. For example, table 2 could contain
 * a pay rate to be changed annually.
 * 
 * If NONE is used, the left table and field and right field are ignored, the only relevant part of the join
 * data is the right report or table
 */
public enum JoinType {
    LEFT_OUTER, INNER, RIGHT_OUTER, FULL_OUTER, NONE;

    public String toString() {
        return super.toString().replaceAll("_", " ");
    }
}

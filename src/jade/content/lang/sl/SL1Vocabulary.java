/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * <p>
 * GNU Lesser General Public License
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.content.lang.sl;

/**
 * The vocabulary of the simbols used in the FIPA SL1 language
 *
 * @author Giovanni Caire - TILAB
 */
public interface SL1Vocabulary extends SL0Vocabulary {
    String AND = "and";
    String AND_LEFT = "left";
    String AND_RIGHT = "right";

    String OR = "or";
    String OR_LEFT = "left";
    String OR_RIGHT = "right";

    String NOT = "not";
    String NOT_WHAT = "what";
}

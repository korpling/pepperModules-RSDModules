/*
 * Copyright 2016 GU.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.corpus_tools.pepper_RSDModule;
import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;


/**
 *
 * @author Amir Zeldes
 */
public class RSDImporterProperties extends PepperModuleProperties  {


	public static final String PREFIX = "RSD.";


        public final static String NAMESPACE = PREFIX + "namespace";
	public final static String EDGETYPE = PREFIX + "edgeType";
	public final static String EDGEANNONAME = PREFIX + "edgeAnnoName";
	public final static String ADDEDUNUM = PREFIX + "addEduNum";
	public final static String ADDRELANNO = PREFIX + "addRelAnno";

	public RSDImporterProperties() {
		this.addProperty(new PepperModuleProperty<String>(NAMESPACE, String.class, "Specifies a namespace to assign to all imported annotations.", "rsd", false));
		this.addProperty(new PepperModuleProperty<String>(EDGETYPE, String.class, "Specifies an edge type for rsd relations.", "rsd", false));
		this.addProperty(new PepperModuleProperty<String>(EDGEANNONAME, String.class, "Specifies an edge annotation name for rsd relations.", "func", false));
		this.addProperty(new PepperModuleProperty<Boolean>(ADDEDUNUM, Boolean.class, "Whether to add EDU numbers", false, false));
		this.addProperty(new PepperModuleProperty<Boolean>(ADDRELANNO, Boolean.class, "Whether to also add the rhetorical relation as a node annotation to the EDU span.", false, false));
        }

    
}

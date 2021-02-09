package org.corpus_tools.pepper_RSDModule;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Iterator;
import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleDataException;
import org.corpus_tools.peppermodules.conll.tupleconnector.TupleConnectorFactory;
import org.corpus_tools.peppermodules.conll.tupleconnector.TupleReader;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SLayer;
import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 *
 * @author Amir Zeldes
 */
public class RSD2SaltMapper extends PepperMapperImpl{
    
    private String namespace;
    private String edgeType;
    private String edgeAnnoName;
    private boolean addEduNum;
    private boolean addRel;
    private SLayer layer;

    /**
     * Mapper for RSD format to Salt
     * 
     */

    public RSD2SaltMapper()
    {
        setProperties(new RSDImporterProperties());

    }
    
        private static final Logger logger = LoggerFactory.getLogger(RSD2SaltMapper.class);

        @Override
        public DOCUMENT_STATUS mapSCorpus() {

                return (DOCUMENT_STATUS.COMPLETED);
        }

        /**
         * {@inheritDoc PepperMapper#setDocument(SDocument)}
         * 
         */
        @Override
        public DOCUMENT_STATUS mapSDocument() {

                getDocument().setDocumentGraph(SaltFactory.createSDocumentGraph());


                // assign customizationn values from importer properties
                this.namespace = (String) getProperties().getProperties().getProperty(RSDImporterProperties.NAMESPACE, "rsd");                 
                if (this.namespace != null){
                    this.layer = SaltFactory.createSLayer();
                    this.layer.setName(this.namespace);
                    this.layer.setGraph(getDocument().getDocumentGraph());
                }
                this.edgeType = (String) getProperties().getProperties().getProperty(RSDImporterProperties.EDGETYPE, "rsd");                 
                this.edgeAnnoName = (String) getProperties().getProperties().getProperty(RSDImporterProperties.EDGEANNONAME, "func");                 
                if (getProperties().getProperty(RSDImporterProperties.ADDEDUNUM).getValue() instanceof Boolean){
                    this.addEduNum = (Boolean) getProperties().getProperty(RSDImporterProperties.ADDEDUNUM).getValue();                                                        
                }
                else{
                    this.addEduNum = Boolean.valueOf((String) getProperties().getProperty(RSDImporterProperties.ADDEDUNUM).getValue());                                                                                    
                }
                if (getProperties().getProperty(RSDImporterProperties.ADDRELANNO).getValue() instanceof Boolean){
                    this.addRel = (Boolean) getProperties().getProperty(RSDImporterProperties.ADDRELANNO).getValue();                                                        
                }
                else{
                    this.addRel = Boolean.valueOf((String) getProperties().getProperty(RSDImporterProperties.ADDRELANNO).getValue());                                                                                    
                }

                // to get the exact resource which is processed now, call
                // getResources(), make sure, it was set in createMapper()
                URI resource = getResourceURI();

                // we record, which file currently is imported to the debug stream,
                // in this dummy implementation the resource is null
                logger.debug("Importing the file {}.", resource.toFileString());


        TupleReader tupleReader = TupleConnectorFactory.fINSTANCE.createTupleReader();
        // try reading the input file
        try {

                tupleReader.setSeperator("\t");
                tupleReader.setFile(new File(this.getResourceURI().toFileString()));
                tupleReader.readFile();
        } 
        catch (IOException e) {
                String errorMessage = "Input file could not be read. Aborting conversion of file " + this.getResourceURI() + ".";
                logger.error(errorMessage);
                throw new PepperModuleDataException(this, errorMessage);
        }

        STextualDS sTextualDS = SaltFactory.createSTextualDS();
        sTextualDS.setGraph(getDocument().getDocumentGraph());

        HashMap<String,String> child2parent = new LinkedHashMap<>();
        HashMap<String,SSpan> id2edu = new LinkedHashMap<>();
        HashMap<String,String> id2rel = new LinkedHashMap<>();

        Collection<String> tuple = null;
        int numOfTuples = tupleReader.getNumOfTuples();
        int tupleSize;
        int fieldNum = 1;
        float processedTuples = 0;

        // variables to keep track of tokens and text                
        // using a StringBuilder for the iteratively updated raw text
        int stringBuilderCharBufferSize = tupleReader.characterSize(2) + numOfTuples;
        StringBuilder primaryText = new StringBuilder(stringBuilderCharBufferSize);
        String tokID;
        String textOffset; // TODO: properly decode text offsets to reconstruct whitespace preserving STextualDS
        String eduText;
        String eduID;
        String annoVal;
        String annoField;
        int annoIndex;


        // iteration over all data rows (the complete input file)
        for (int rowIndex = 0; rowIndex < numOfTuples; rowIndex++) {
                try {
                        tuple = tupleReader.getTuple();
                } catch (IOException e) {
                        String errorMessage = String.format("line %d of input file could not be read. Abort conversion of file " + this.getResourceURI() + ".", rowIndex + 1);
                        throw new PepperModuleDataException(this, errorMessage);
                }

                tupleSize = tuple.size();

                if (tupleSize == 1) {
                    // Assume some sort of comment line, do nothing
                       
                }
                else if (tupleSize == 10){ // EDU row in 10 column format

                    // Extend the text - currently adding space after each token
                    // TODO: get actual whitespace from #text comment

                    // Create the token and index it in the token list
                    Iterator<String> iter = tuple.iterator();
                    eduID = iter.next();
                    eduText = iter.next();
                    iter.next();
                    iter.next();
                    iter.next();
                    String feats = iter.next();
                    String parentID = iter.next();
                    String rsdRel = iter.next();

                    // create SSpan for edu
                    SSpan edu = SaltFactory.createSSpan();
                    edu.setGraph(getDocument().getDocumentGraph());
                    if (this.namespace != null){
                        edu.addLayer(this.layer);
                    }
                    
                    // create tokens and add to token list
                    String[] toks = eduText.split(" ");
                    for (String tok: toks){
                        // update primary text string builder (sTextualDS.sText will be set after
                        // completely reading the input file)
                        int tokenTextStartOffset = primaryText.length();
                        primaryText.append(tok).append(" ");
                        int tokenTextEndOffset = primaryText.length() - 1;
                        
                        SToken sToken = SaltFactory.createSToken();
                        sToken.setGraph(getDocument().getDocumentGraph());                        
                        
                        // create textual relation
                        STextualRelation sTextualRelation = SaltFactory.createSTextualRelation();
                        sTextualRelation.setSource(sToken);
                        sTextualRelation.setTarget(sTextualDS);
                        sTextualRelation.setStart(tokenTextStartOffset);
                        sTextualRelation.setEnd(tokenTextEndOffset);
                        sTextualRelation.setGraph(getDocument().getDocumentGraph());
                        
                        // connect token to SSpan
                        SSpanningRelation spanrel = SaltFactory.createSSpanningRelation();
                        spanrel.setSource(edu);
                        spanrel.setTarget(sToken);
                        spanrel.setGraph(getDocument().getDocumentGraph());
                    }
                    
                    if (this.addEduNum){
                        if (feats.length() > 0){
                            feats += "|";
                        }                        
                        feats += "edu_num=" + eduID;
                    }
                    if (this.addRel){
                        if (feats.length() > 0){
                            feats += "|";
                        }                        
                        feats += "rsd_rel=" + rsdRel;
                    }
                    
                    // add feature annotations
                    String[] featlist = feats.split("\\|");
                    for (String feat: featlist){
                        if (feat.indexOf("=")>0 && feat.indexOf("=")<feat.length()-1){
                            String[] keyval = feat.split("=");
                            SAnnotation anno = SaltFactory.createSAnnotation();
                            anno.setName(keyval[0]);
                            anno.setValue(keyval[1]);
                            if (this.namespace != null){
                                anno.setNamespace(this.namespace);
                            }
                            edu.addAnnotation(anno);
                        }
                    }
                    
                    // Store deprel and id
                    id2edu.put(eduID,edu);
                    child2parent.put(eduID,parentID);
                    id2rel.put(eduID,rsdRel);

            }                                    
                // Finished reading an input tuple
                //processedTuples++;                        
                if ((int)((rowIndex / numOfTuples)*100)%10 == 0){
                    addProgress((double)(rowIndex / numOfTuples));
                }
        }

            // ### file is completely read now ###

            // delete last char of primary text (a space character) and set it as
            // text for STextualDS
            primaryText.deleteCharAt(primaryText.length() - 1);
            sTextualDS.setText(primaryText.toString());


            // create SPointingRelations between SSpans if found
            for (String childID : child2parent.keySet()) {
                
                String parentID = child2parent.get(childID);
                if (!(parentID.equals("0"))){
                    // Check that source and target are not identical
                    if (!childID.equals(parentID)) {

                        if (!(id2edu.containsKey(childID))){
                            throw new PepperModuleDataException(this,"Input error: relation with missing source element: " + childID + "\n" );
                        }
                        if (!(id2edu.containsKey(parentID))){
                            throw new PepperModuleDataException(this,"Input error: relation with missing target element: " + parentID + "\n" );
                        }

                        SPointingRelation sRel = SaltFactory.createSPointingRelation();
                        sRel.setSource(id2edu.get(childID));
                        sRel.setTarget(id2edu.get(parentID));
                        sRel.setType(this.edgeType);
                        SAnnotation relAnno = SaltFactory.createSAnnotation();
                        if (namespace != null){
                            relAnno.setNamespace(namespace);
                            sRel.addLayer(this.layer);
                        }
                        relAnno.setName(this.edgeAnnoName);
                        relAnno.setValue(id2rel.get(childID));
                        sRel.addAnnotation(relAnno);
                        getDocument().getDocumentGraph().addRelation(sRel);
                    }
                }
            }
        

        return (DOCUMENT_STATUS.COMPLETED);

    }                                    

}



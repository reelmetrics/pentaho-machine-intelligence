/*******************************************************************************
 * Pentaho Data Science
 * <p/>
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 * <p/>
 * ******************************************************************************
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.pentaho.di.trans.steps.pmi;

import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.dm.commons.LogAdapter;
import weka.classifiers.Classifier;
import weka.clusterers.Clusterer;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.pmml.PMMLModel;

import java.io.Serializable;

/**
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: $
 */
public abstract class PMIScoringModel implements Serializable {

  /**
   * Attribute (header) information for the training Instances used to create the model being wrapped
   */
  protected Instances m_header;

  /**
   * Creates a new <code>PMIScoringModel</code> instance.
   *
   * @param model the actual Weka (wrapped) model to enacpsulate
   */
  public PMIScoringModel( Object model ) {
    setModel( model );
  }

  /**
   * Set the log to pass on to the model. Only PMML models require logging.
   *
   * @param log the log to use
   */
  public void setLog( LogChannelInterface log ) {
    if ( getModel() instanceof PMMLModel ) {
      LogAdapter logger = new LogAdapter( log );
      ( (PMMLModel) getModel() ).setLog( logger );
    }
  }

  /**
   * Set the Instances header
   *
   * @param header an <code>Instances</code> value
   */
  public void setHeader( Instances header ) {
    m_header = header;
  }

  /**
   * Get the header of the Instances that was used build this Weka model
   *
   * @return an <code>Instances</code> value
   */
  public Instances getHeader() {
    return m_header;
  }

  /**
   * Tell the model that this scoring run is finished.
   */
  public void done() {
    // subclasses override if they need to do
    // something here.
  }

  /**
   * Set the weka model
   *
   * @param model the Weka model
   */
  public abstract void setModel( Object model );

  /**
   * Get the weka model
   *
   * @return the Weka model as an object
   */
  public abstract Object getModel();

  /**
   * Return a classification. What this represents depends on the implementing
   * sub-class. It could be the index of a class-value, a numeric value or a
   * cluster number for example.
   *
   * @param inst the Instance to be classified (predicted)
   * @return the prediction
   * @throws Exception if an error occurs
   */
  public abstract double classifyInstance( Instance inst ) throws Exception;

  /**
   * Return a probability distribution (over classes or clusters).
   *
   * @param inst the Instance to be predicted
   * @return a probability distribution
   * @throws Exception if an error occurs
   */
  public abstract double[] distributionForInstance( Instance inst ) throws Exception;

  /**
   * Batch scoring method. Call isBatchPredictor() first in order to determine
   * if the underlying model can handle batch scoring.
   *
   * @param insts the instances to score
   * @return an array of predictions
   * @throws Exception if a problem occurs
   */
  public abstract double[] classifyInstances( Instances insts ) throws Exception;

  /**
   * Batch scoring method. Call isBatchPredictor() first in order to determine
   * if the underlying model can handle batch scoring.
   *
   * @param insts the instances to score
   * @return an array of probability distributions, one for each instance
   * @throws Exception if a problem occurs
   */
  public abstract double[][] distributionsForInstances( Instances insts ) throws Exception;

  /**
   * Returns true if the encapsulated Weka model is a supervised model (i.e. has
   * been built to predict a single target in the data).
   *
   * @return true if the encapsulated Weka model is a supervised model
   */
  public abstract boolean isSupervisedLearningModel();

  /**
   * Returns true if the encapsulated Weka model can be updated incrementally in
   * an instance by instance fashion.
   *
   * @return true if the encapsulated Weka model is incremental model
   */
  public abstract boolean isUpdateableModel();

  /**
   * Returns true if the encapsulated Weka model can produce predictions in a
   * batch.
   *
   * @return true if the encapsulated Weka model can produce predictions in a
   * batch
   */
  public abstract boolean isBatchPredictor();

  /**
   * Update (if possible) a model with the supplied Instance
   *
   * @param inst the Instance to update the model with
   * @return true if the model was updated
   * @throws Exception if an error occurs
   */
  public abstract boolean update( Instance inst ) throws Exception;

  /**
   * Static factory method to create an instance of an appropriate subclass of
   * WekaScoringModel given a Weka model.
   *
   * @param model a Weka model
   * @return an appropriate WekaScoringModel for this type of Weka model
   * @throws Exception if an error occurs
   */
  public static PMIScoringModel createScorer( Object model ) throws Exception {
    if ( model instanceof Classifier ) {
      return new PMIScoringClassifier( model );
    } else if ( model instanceof Clusterer ) {
      return new PMIScoringClusterer( model );
    }
    throw new Exception( "Unsupported model type: " + model.getClass().getCanonicalName() );
  }

}

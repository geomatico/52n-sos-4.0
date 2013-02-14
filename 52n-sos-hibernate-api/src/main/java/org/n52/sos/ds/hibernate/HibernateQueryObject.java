/**
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.sos.ds.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.transform.ResultTransformer;

public class HibernateQueryObject implements Cloneable {
    
    private Map<String, String> aliases = new HashMap<String, String>(0);
    
    private List<Criterion> criterions = new ArrayList<Criterion>(0);
    
    private List<Projection> projections = new ArrayList<Projection>(0);
    
    private Order order;
    
    private int maxResult = -1;
    
    private ResultTransformer resultTransformer;
    
    public Map<String, String> getAliases() {
        return aliases;
    }

    public List<Criterion> getCriterions() {
        return criterions;
    }

    public List<Projection> getProjections() {
        return projections;
    }

    public Order getOrder() {
        return order;
    }

    public int getMaxResult() {
        return maxResult;
    }
    
    public void setAliases(Map<String, String> aliases) {
       this.aliases.putAll(aliases);
    }

    public void setCriterions(List<Criterion> criterions) {
        this.criterions.addAll(criterions);
    }
    
    public void addCriterion(Criterion criterion) {
        this.criterions.add(criterion);
    }

    public void setProjections(List<Projection> projections) {
        this.projections.addAll(projections);
    }
    
    public void addProjection(Projection projection) {
        this.projections.add(projection);
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setMaxResult(int maxResult) {
        this.maxResult = maxResult;
    }
    
    public boolean isSetAliases() {
        return aliases != null && !aliases.isEmpty();
    }

    public boolean isSetCriterions() {
        return criterions != null && !criterions.isEmpty();
    }
    
    public boolean isSetProjections() {
        return projections != null && !projections.isEmpty();
    }
    
    public boolean isSetOrder() {
        return order != null;
    }
    
    public boolean isSetMaxResults() {
        return maxResult > 0;
    }

    @Override
    public HibernateQueryObject clone() {
        HibernateQueryObject copy = new HibernateQueryObject();
        copy.setAliases(new HashMap<String, String>(this.aliases));
        copy.setCriterions(new ArrayList<Criterion>(this.criterions));
        copy.setProjections(new ArrayList<Projection>(this.projections));
        copy.setMaxResult(this.maxResult);
        copy.setOrder(this.order);
        return copy;
    }

	@Override
	public String toString()
	{
		return String.format("HibernateQueryObject [aliases=%s, criterions=%s, projections=%s, order=%s, maxResult=%s]", 
				aliases, criterions, projections, order, maxResult);
	}

    public void setResultTransformer(ResultTransformer resultTransformer) {
        this.resultTransformer = resultTransformer;
    }
    

    public ResultTransformer getResultTransformer() {
        return resultTransformer;
    }
   
    public boolean isSetResultTransformer() {
        return resultTransformer != null;
    }
    
}

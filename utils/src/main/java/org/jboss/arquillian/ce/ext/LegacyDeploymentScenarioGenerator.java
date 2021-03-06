/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.arquillian.ce.ext;

import static org.jboss.arquillian.ce.utils.Containers.isDeployedInCeContainer;

import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.ce.utils.Archives;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentScenarioGenerator;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Rewrite any .jar deployment into .war.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class LegacyDeploymentScenarioGenerator implements DeploymentScenarioGenerator {
    private final static String DELEGATE_CLASS = "org.jboss.arquillian.container.test.impl.client.deployment.AnnotationDeploymentScenarioGenerator";

    public List<DeploymentDescription> generate(TestClass testClass) {
        List<DeploymentDescription> descriptions = delegate(testClass);

        boolean inCeContainer = isDeployedInCeContainer();

        List<DeploymentDescription> copy = new ArrayList<>();
        for (DeploymentDescription description : descriptions) {
            Archive<?> archive = description.getArchive();
            // only wrap in war, if it's in CE container
            if (inCeContainer && JavaArchive.class.isInstance(archive)) {
                JavaArchive jar = JavaArchive.class.cast(archive);
                copy.add(new DeploymentDescription(description.getName(), toWar(jar)));
            } else {
                copy.add(description);
            }
        }
        return copy;
    }

    private WebArchive toWar(JavaArchive jar) {
        WebArchive war = Archives.generateDummyWebArchive();
        merge(war, jar);
        return war;
    }

    private void merge(WebArchive war, JavaArchive jar) {
        war.addAsLibraries(jar);
    }

    private List<DeploymentDescription> delegate(TestClass testClass) {
        try {
            DeploymentScenarioGenerator delegate = (DeploymentScenarioGenerator) Class.forName(DELEGATE_CLASS).newInstance();
            return delegate.generate(testClass);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}

/*
* Copyright (C) 2003-2015 eXo Platform SAS.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see http://www.gnu.org/licenses/ .
*/
package org.exoplatform.commons.api.persistence;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * At compile time, every time a Maven module that uses @ExoEntity is compiled, the ExoEntityProcessor is invoked.
 * This Annotation Processor (see JSR 269) creates an index that contains the list of all
 * the JPA entities annotated with @ExoEntity of the module.
 * This index is stored in the file “exo-jpa-entities/entities.idx” of the generated jar.
 *
 * Created by The eXo Platform SAS
 * Author : eXoPlatform exo@exoplatform.com
 * 7/22/15
 */
@SupportedAnnotationTypes("org.exoplatform.commons.api.persistence.ExoEntity")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ExoEntityProcessor extends AbstractProcessor {
  /** Path to the generated entities.idx file **/
  public static final String ENTITIES_IDX_PATH = "exo-jpa-entities/entities.idx";

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Writer writer = null;
    if (!roundEnv.processingOver()) {
      try {
        FileObject file = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", ENTITIES_IDX_PATH);
        writer = file.openWriter();
        for (Element element : roundEnv.getElementsAnnotatedWith(ExoEntity.class)) {
          writer.append(element.asType().toString() + "\n");
          processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Exo JPA entity detected: " + element.asType());
        }
      } catch (IOException e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error while processing @ExoEntity: " + e.getMessage());
      } finally {
        if (writer != null) {
          try {
            writer.close();
          } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error while processing @ExoEntity: " + e.getMessage());
          }
        }
      }
    }
    return true; // No need to process these annotations again
  }
}

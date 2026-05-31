/*
 * Copyright © 2012 ecuacion.jp (info@ecuacion.jp)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.ecuacion.tool.codegenerator.web.controller;

import jp.ecuacion.splib.web.controller.SplibGeneral1FormController;
import jp.ecuacion.tool.codegenerator.web.form.SourceDownloadForm;
import jp.ecuacion.tool.codegenerator.web.service.SourceDownloadService;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Handles HTTP requests for the source code download feature. */
@Controller
@Scope("prototype")
@RequestMapping("/public/sourceDownload")
public class SourceDownloadController
    extends SplibGeneral1FormController<SourceDownloadForm, SourceDownloadService> {

  /** Constructs a new instance. */
  public SourceDownloadController() {
    super("sourceDownload");

  }

  /** Downloads the generated source code as a ZIP file. */
  @PostMapping(value = "action", params = "action=download")
  public ResponseEntity<Resource> download(Model model, @Validated SourceDownloadForm form,
      BindingResult result) throws Exception {
    prepare(model, form);

    ResponseEntity<Resource> responseEntity =
        getService().execute(form.getSourceDownload().getFileToUpload());

    addCookieForDownloadButton();

    return responseEntity;
  }
}

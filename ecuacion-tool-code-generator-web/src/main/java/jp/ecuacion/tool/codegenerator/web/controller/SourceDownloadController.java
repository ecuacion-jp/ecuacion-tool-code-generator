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

@Controller
@Scope("prototype")
@RequestMapping("/public/sourceDownload")
public class SourceDownloadController
    extends SplibGeneral1FormController<SourceDownloadForm, SourceDownloadService> {

  public SourceDownloadController() {
    super("sourceDownload");

  }

  @PostMapping(value = "action", params = "sourceDownloadButton")
  public ResponseEntity<Resource> download(Model model, @Validated SourceDownloadForm form,
      BindingResult result) throws Exception {
    prepare(model, form);

    return getService().execute(form.getSourceDownload().getFileToUpload());
  }
}

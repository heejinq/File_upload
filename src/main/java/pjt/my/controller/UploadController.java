package pjt.my.controller;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
//import javax.annotation.Resource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.log4j.Log4j;
import net.coobird.thumbnailator.Thumbnailator;
import pjt.my.domain.AttachFileDTO;

@Controller
@Log4j
public class UploadController {
	
	
	@GetMapping("/uploadForm") //업로드페이지 이동
	public void uploadForm() {
		log.info("upload Form");
	}
	
	@PostMapping("/uploadFormAction") //업로드 처리 기능
	public void uploadFormAction(MultipartFile[] uploadFile, Model model) {
		String uploadFolder = "C:\\DEV\\sts_workspace\\uploadfile";
		for(MultipartFile multipartfile : uploadFile) {
			log.info(multipartfile.getOriginalFilename() + "***" + multipartfile.getSize());
			File saveFile = new File(uploadFolder, multipartfile.getOriginalFilename());
			try {
				multipartfile.transferTo(saveFile);
			}catch (Exception e) {
				log.error(e.getMessage());
			}
			}
		}
//	ajax방식 ajax로 업로드 페이지로 이동
	@GetMapping("/uploadAjax")
	public void uploadAjax() {
		log.info("uploadAjax");
	}
	
	
	//이름 중복 처리를 위한 폴더
	private String getFolder() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String str = sdf.format(date);
		return str.replace("-", File.separator);
		}
	//섬네일 처리를 위한 부분
	private boolean checkImgType(File file) {
		try {
			String contentType = Files.probeContentType(file.toPath());
			return contentType.startsWith("image");
		}catch(IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@PostMapping(value = "/uploadAjaxAction",produces = MediaType.APPLICATION_JSON_UTF8_VALUE) //ajax로 업로드
	@ResponseBody
	public ResponseEntity<List<AttachFileDTO>>
	uploadAjaxPost(MultipartFile[] uploadFile) {
		
		List<AttachFileDTO> list = new ArrayList<>();
		log.info("post update");
		String uploadFolder = "C:\\DEV\\sts_workspace\\uploadfile";
		
		String uploadFolderPath = getFolder();
		
		//이름 중복 처리를 위한 폴더만들기
		File uploadPath = new File(uploadFolder,getFolder());
		log.info("uploadpath : "+uploadPath);
		if(uploadPath.exists() == false) {
			uploadPath.mkdirs();
		}
		
		
		for(MultipartFile multipartfile : uploadFile) {
			AttachFileDTO attachDTO = new AttachFileDTO();
			log.info("--------------file name : "+multipartfile.getOriginalFilename());
			log.info("file size"+multipartfile.getSize());
			
			String uploadFileName = multipartfile.getOriginalFilename();
			
			uploadFileName = uploadFileName.substring(uploadFileName.lastIndexOf("\\") + 1);
			log.info(uploadFileName);
			attachDTO.setFileName(uploadFileName);
			
			UUID uuid = UUID.randomUUID();
			uploadFileName = uuid.toString() + "-" +uploadFileName;
			
			try {
				File saveFile = new File(uploadPath, uploadFileName);
				multipartfile.transferTo(saveFile);
				
				attachDTO.setUuid(uuid.toString());
				attachDTO.setUploadPath(uploadFolderPath);
				
				//섬네일 부분
				if(checkImgType(saveFile)) {
					attachDTO.setImage(true);
					FileOutputStream thumbnail = new FileOutputStream(new File(uploadPath,"sn_"+uploadFileName));
					Thumbnailator.createThumbnail(multipartfile.getInputStream(),thumbnail,100,100);
					thumbnail.close();
				}
				list.add(attachDTO);
			}catch(Exception e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
		}
		return new ResponseEntity<>(list,HttpStatus.OK);
	}
	
	@GetMapping("/display")
	@ResponseBody
	public ResponseEntity<byte[]> getFile(String fileName){
		log.info("filename : "+fileName);
		File file = new File("C:\\DEV\\sts_workspace\\uploadfile\\"+fileName);
		log.info("file : "+file);
		ResponseEntity<byte[]> result = null;
		try {
			HttpHeaders header = new HttpHeaders();
			header.add("Content-Type", Files.probeContentType(file.toPath()));
			result = new ResponseEntity<>(FileCopyUtils.copyToByteArray(file),header,HttpStatus.OK);
		}catch(IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
//	@GetMapping(value = "/download",produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
//	@ResponseBody
//	public ResponseEntity<Resource> downloadFile(String fileName){
//		log.info("다운로드 파일 : "+fileName);
//		FileSystemResource resource = new FileSystemResource("C:\\DEV\\sts_workspace\\uploadfile\\"+fileName);
//		log.info("resource"+resource);
//		return null;
//	}
	
	@GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody
		public ResponseEntity<Resource> downloadFile(@RequestHeader("User-Agent") String userAgent,String fileName){
		Resource resource = new FileSystemResource("C:\\DEV\\sts_workspace\\uploadfile\\"+fileName);
		log.info("resource"+resource);
		
		if(resource.exists() == false) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		String resourceName = resource.getFilename();
//		String resourceOriginalName = resourceName.substring(37);
		String resourceOriginalName = resourceName.substring(resourceName.indexOf("-") + 1); 
//		String resourceOriginalName = resourceName.substring(resourceName.indexOf("_") + 1); 
		HttpHeaders headers = new HttpHeaders();
		try {
			String downloadName = null;
			if(userAgent.contains("Trident")) {
				log.info("IE");
				downloadName = URLEncoder.encode(resourceOriginalName,"UTF-8").replaceAll("\\", " ");
			}else if(userAgent.contains("Edge")) {
				log.info("Edge");
				downloadName = URLEncoder.encode(resourceOriginalName,"UTF-8");
			}else {
				log.info("CHROME");
				downloadName = new String(resourceOriginalName.getBytes("UTF-8"),"ISO-8859-1");
			}
			log.info("downloadName" + downloadName);
			headers.add("Content-Disposition", "attachment; filename=" +downloadName);
		}catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Resource>(resource,headers,HttpStatus.OK); 
	}
	
	@PostMapping("/deleteFile")
	@ResponseBody
	public ResponseEntity<String> deleteFile(String fileName,String type){
		log.info("deleteFile : "+fileName);
		File file;
		try {
			file = new File("C:\\DEV\\sts_workspace\\uploadfile\\"+URLDecoder.decode(fileName, "UTF-8"));
			file.delete();
			if(type.equals("image")) {
				String largeFileName = file.getAbsolutePath().replace("sn_", "");
				log.info("largeFileName"+largeFileName);
				file = new File(largeFileName);
				file.delete();
			}
		}catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<String>("deleted!",HttpStatus.OK);
	}

	
	}
	


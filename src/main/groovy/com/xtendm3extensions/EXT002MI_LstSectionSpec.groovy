// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list section species from EXTCSS
// Transaction LstSectionSpec
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DSID - Section ID
 * @param: SPEC - Species
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DSID - Section ID
 * @return: SPEC - Species
 * @return: SSID - Species ID
 * 
*/

public class LstSectionSpec extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inDSID
  String inSPEC
  int numberOfFields
  
  // Constructor 
  public LstSectionSpec(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
     this.mi = mi
     this.database = database 
     this.program = program
  } 
    
  public void main() { 
     // Set Company Number
     if (mi.in.get("CONO") != null) {
        inCONO = mi.in.get("CONO") 
     } else {
        inCONO = 0      
     }

     // Set Division
     if (mi.in.get("DIVI") != null) {
        inDIVI = mi.inData.get("DIVI").trim() 
     } else {
        inDIVI = ""     
     }
    
     // Section ID
     if (mi.in.get("DSID") != null) {
        inDSID = mi.in.get("DSID") 
     } else {
        inDSID = 0     
     }
     
     // Species
     if (mi.in.get("SPEC") != null) {
        inSPEC = mi.inData.get("SPEC").trim() 
     } else {
        inSPEC = ""     
     }
     

     // List section species
     listSectionSpecies()
  }
 
  //******************************************************************** 
  // List Contract Status from EXTCSS
  //******************************************************************** 
  void listSectionSpecies(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTCSS")

     numberOfFields = 0

     if (inCONO != 0) {
       numberOfFields = 1
       expression = expression.eq("EXCONO", String.valueOf(inCONO))
     }

     if (inDIVI != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXDIVI", inDIVI))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXDIVI", inDIVI)
         numberOfFields = 1
       }
     }

     if (inSPEC != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXSPEC", inSPEC))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXSPEC", inSPEC)
         numberOfFields = 1
       }
     }

     if (inDSID != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXDSID", String.valueOf(inDSID)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXDSID", String.valueOf(inDSID))
         numberOfFields = 1
       }
     }

     DBAction actionline = database.table("EXTCSS").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()     
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               
  }

  Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("DSID", line.get("EXDSID").toString())
      mi.outData.put("SPEC", line.getString("EXSPEC"))
      mi.outData.put("SSID", line.get("EXSSID").toString())
      mi.write() 
  } 
   
}
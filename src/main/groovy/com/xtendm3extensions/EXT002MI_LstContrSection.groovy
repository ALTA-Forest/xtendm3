// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list contract sections from EXTCDS
// Transaction LstContrSection
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RVID - Revision ID
 * @param: DSID - Section ID
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: RVID - Revision ID
 * @return: CSSN - Section Number
 * @return: DSID - Section ID
 * @return: CSNA - Section Name
 * @return: DPOR - Display Order
 * @return: CSSP - Species
 * @return: CSGR - Grades
 * @return: CSEX - Exception Codes
 * @return: CSLI - Length Increment
 * 
*/

public class LstContrSection extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  String inRVID
  int inCSSN
  int numberOfFields
  
  // Constructor 
  public LstContrSection(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
     if (mi.in.get("DIVI") != null && mi.in.get("DIVI") != "") {
        inDIVI = mi.inData.get("DIVI").trim() 
     } else {
        inDIVI = ""     
     }
 
     // Revision ID
     if (mi.in.get("RVID") != null && mi.in.get("RVID") != "") {
        inRVID = mi.inData.get("RVID").trim() 
     } else {
        inRVID = ""     
     }

     // Section Number
     if (mi.in.get("CSSN") != null) {
        inCSSN = mi.in.get("CSSN") 
     } else {
        inCSSN = 0     
     }


     // List contract status
     listContractSections()
  }
 
  //******************************************************************** 
  // List Contract Status from EXTCDS
  //******************************************************************** 
  void listContractSections(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTCDS")

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

     if (inRVID != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXRVID", inRVID))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXRVID", inRVID)
         numberOfFields = 1
       }
     }

     if (inCSSN != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXCSSN", String.valueOf(inCSSN)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXCSSN", String.valueOf(inCSSN))
         numberOfFields = 1
       }
     }

     DBAction actionline = database.table("EXTCDS").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()  
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               
  }


  Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("RVID", line.getString("EXRVID"))
      mi.outData.put("CSSN", line.get("EXCSSN").toString())
      mi.outData.put("DSID", line.get("EXDSID").toString())
      mi.outData.put("CSNA", line.getString("EXCSNA"))
      mi.outData.put("DPOR", line.get("EXDPOR").toString())
      mi.outData.put("CSSP", line.get("EXCSSP").toString())
      mi.outData.put("CSGR", line.get("EXCSGR").toString())
      mi.outData.put("CSEX", line.get("EXCSEX").toString())
      mi.outData.put("CSLI", line.get("EXCSLI").toString())
      mi.outData.put("FRSC", line.get("EXFRSC").toString())
      mi.write() 
  } 
   
}
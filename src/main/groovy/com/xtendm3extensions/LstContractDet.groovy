// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list contract details from EXTCTD
// Transaction LstContractDet
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: CTNO - Contract Number
 * @param: RVID - Revision ID
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: RVNO - Revision Number
 * @return: CTNO - Contract Number
 * @return: VALF - Valid From
 * @return: VALT - Valid To
 * @return: STAT - Status
 * @return: PTPC - Permit Type
 * @return: RTPC - Rate Type
 * @return: TEPY - Payment Terms
 * @return: FRSC - Frequency Scaling
 * @return: RVID - Revision ID
 * @return: CMNO - Compliance Number
 * @return: PTDT - Permit Date
*/

public class LstContractDet extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inCTNO
  String inRVID
  int numberOfFields
  
  // Constructor 
  public LstContractDet(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database 
     this.program = program
     this.logger = logger
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
    
     // Contract Number
     if (mi.in.get("CTNO") != null) {
        inCTNO = mi.in.get("CTNO") 
     } else {
        inCTNO = 0      
     }

     // Revision ID
     if (mi.in.get("RVID") != null) {
        inRVID = mi.inData.get("RVID").trim() 
     } else {
        inRVID = ""     
     }

     // List contracts from EXTCTD
     listContractDetails()
  }


  //******************************************************************** 
  // List records
  //********************************************************************  
   void listContractDetails(){   
     ExpressionFactory expression = database.getExpressionFactory("EXTCTD")

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

     if (inCTNO != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXCTNO", String.valueOf(inCTNO)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXCTNO", String.valueOf(inCTNO))
         numberOfFields = 1
       }
     }

     DBAction actionline = database.table("EXTCTD").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()       
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               
   } 
    
  //******************************************************************** 
  // List Contract Detail Lines
  //********************************************************************  
  Closure<?> releasedLineProcessor = { DBContainer line ->   
      mi.outData.put("CONO", line.get("EXCONO").toString()) 
      mi.outData.put("DIVI", line.getString("EXDIVI")) 
      mi.outData.put("RVNO", line.get("EXRVNO").toString())
      mi.outData.put("CTNO", line.get("EXCTNO").toString())
      mi.outData.put("VALF", line.get("EXVALF").toString())
      mi.outData.put("VALT", line.get("EXVALT").toString())
      mi.outData.put("STAT", line.get("EXSTAT").toString())
      mi.outData.put("PTPC", line.getString("EXPTPC")) 
      mi.outData.put("RTPC", line.getString("EXRTPC")) 
      mi.outData.put("TEPY", line.getString("EXTEPY")) 
      mi.outData.put("FRSC", line.get("EXFRSC").toString())
      mi.outData.put("RVID", line.getString("EXRVID")) 
      mi.outData.put("CMNO", line.getString("EXCMNO"))
      mi.outData.put("PTDT", line.get("EXPTDT").toString())
      mi.write() 
  } 

}
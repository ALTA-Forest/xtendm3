// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list contract trips from EXTCTT
// Transaction LstContractTrip
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: CTNO - Contract Number
 * @param: RVID - Revision ID
 * @param: DLFY - Deliver From Yard
 * @param: DLTY - Deliver To Yard
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: CTNO - Contract Number
 * @return: RVID - Revision ID
 * @return: DLFY - Deliver From Yard
 * @return: DLTY - Deliver To Yard
 * @return: TRRA - Trip Rate
 * @return: MTRA - Minimum Amount
 * 
*/


public class LstContractTrip extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inCTNO
  String inRVID
  String inDLFY
  String inDLTY
  int numberOfFields
  
  // Constructor 
  public LstContractTrip(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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

     // Deliver From Yard
     if (mi.in.get("DLFY") != null) {
        inDLFY = mi.inData.get("DLFY").trim() 
     } else {
        inDLFY = ""      
     }

     // Deliver To Yard
     if (mi.in.get("DLTY") != null) {
        inDLTY = mi.inData.get("DLTY").trim() 
     } else {
        inDLTY = ""      
     }

     // List contracts from EXTCTT
     listContractTrips()
  }
 
  //******************************************************************** 
  // List Contract Trips from EXTCTT
  //******************************************************************** 
  void listContractTrips() { 
     ExpressionFactory expression = database.getExpressionFactory("EXTCTT")

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

     if (inCTNO != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXCTNO", String.valueOf(inCTNO)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXCTNO", String.valueOf(inCTNO))
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

     if (inDLFY != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXDLFY", inDLFY))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXDLFY", inDLFY)
         numberOfFields = 1
       }
     }

     if (inDLTY != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXDLTY", inDLTY))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXDLTY", inDLTY)
         numberOfFields = 1
       }
     }

     DBAction actionline = database.table("EXTCTT").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()    
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               
   } 

    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("CTNO", line.get("EXCTNO").toString())
      mi.outData.put("RVID", line.getString("EXRVID"))
      mi.outData.put("DLFY", line.getString("EXDLFY"))
      mi.outData.put("DLTY", line.getString("EXDLTY"))
      mi.outData.put("TRRA", line.getDouble("EXTRRA").toString())
      mi.outData.put("MTRA", line.getDouble("EXMTRA").toString())
      mi.write()
   } 
   
}
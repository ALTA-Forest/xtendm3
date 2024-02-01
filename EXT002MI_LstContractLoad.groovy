// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-06-07
// @version   1.0 
//
// Description 
// This API is to list contract loads from EXTCTL
// Transaction LstContractLoad
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: CTNO - Contract Number
 * @param: RVID - Revision ID
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DLNO - Delivery Number
 * @return: CTNO - Contract Number
 * @return: RVID - Revision ID
 * @return: TNLB - Total Net lbs
 * @return: TNBF - Total Net bf
 * @return: AVBL - Average bf/lbs
 * @return: AMNT - Amount
 * 
*/

public class LstContractLoad extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inDLNO
  int inCTNO
  String inRVID
  int numberOfFields

  
  // Constructor 
  public LstContractLoad(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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
 
     // Delivery Number
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0     
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
     

     // List contract brands
     listContractInstructions()
  }



  //******************************************************************** 
  // List Contract Loads from EXTCTL
  //******************************************************************** 
  void listContractInstructions(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTCTL")

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

     if (inDLNO != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXDLNO", String.valueOf(inDLNO)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXDLNO", String.valueOf(inDLNO))
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


     DBAction actionline = database.table("EXTCTL").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()      
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               

   } 


    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("DLNO", line.get("EXDLNO").toString())
      mi.outData.put("CTNO", line.get("EXCTNO").toString())
      mi.outData.put("RVID", line.getString("EXRVID"))
      mi.outData.put("TNLB", line.getDouble("EXTNLB").toString())
      mi.outData.put("TNBF", line.getDouble("EXTNBF").toString())
      mi.outData.put("AVBL", line.getDouble("EXAVBL").toString())
      mi.outData.put("AMNT", line.getDouble("EXAMNT").toString())
      mi.write() 
   } 
   
}
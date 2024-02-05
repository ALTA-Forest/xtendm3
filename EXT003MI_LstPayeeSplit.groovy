// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-06-07
// @version   1.0 
//
// Description 
// This API is to list payee split from EXTDPS
// Transaction LstPayeeSplit
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: STID - Scale Ticket ID
 * @param: ITNO - Item Number
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DLNO - Delivery Number
 * @return: STID - Scale Ticket ID
 * @return: ITNO - Item Number
 * @return: SEQN - Sequence
 * @return: CASN - Payee Number
 * @return: SUNM - Payee Name
 * @return: CF15 - Payee Role
 * @return: SUCM - Cost Element
 * @return: INBN - Invoice Batch Number
 * @return: CAAM - Charge Amount
 * 
*/

public class LstPayeeSplit extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inDLNO
  int inSTID
  String inITNO
  int numberOfFields

  
  // Constructor 
  public LstPayeeSplit(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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
        inDIVI = mi.in.get("DIVI") 
     } else {
        inDIVI = ""     
     }

     // Delivery Number
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0     
     }

     // Scale Ticket ID
     if (mi.in.get("STID") != null) {
        inSTID = mi.in.get("STID") 
     } else {
        inSTID = 0     
     }
    
     // Item Number
     if (mi.in.get("ITNO") != null) {
        inITNO = mi.in.get("ITNO") 
     } else {
        inITNO = ""     
     }


     // List payee split
     listPayeeSplit()
  }



  //******************************************************************** 
  // List Payee Split from EXTDPS
  //******************************************************************** 
  void listPayeeSplit(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTDPS")

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

     if (inSTID != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXSTID", String.valueOf(inSTID)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXSTID", String.valueOf(inSTID))
         numberOfFields = 1
       }
     }

     if (inITNO != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXITNO", String.valueOf(inITNO)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXITNO", String.valueOf(inITNO))
         numberOfFields = 1
       }
     }


     DBAction actionline = database.table("EXTDPS").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()             
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               
   } 


    Closure<?> releasedLineProcessor = { DBContainer line -> 
      // Send output value  
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("DLNO", line.get("EXDLNO").toString())
      mi.outData.put("STID", line.get("EXSTID").toString())
      mi.outData.put("ITNO", line.getString("EXITNO"))
      mi.outData.put("SEQN", line.get("EXSEQN").toString())
      mi.outData.put("CASN", line.getString("EXCASN"))
      mi.outData.put("SUNM", line.getString("EXSUNM"))
      mi.outData.put("CF15", line.get("EXCF15").toString())
      mi.outData.put("SUCM", line.getString("EXSUCM"))
      mi.outData.put("INBN", line.get("EXINBN").toString())
      mi.outData.put("CAAM", line.getDouble("EXCAAM").toString())
      mi.write() 
   } 
   
}
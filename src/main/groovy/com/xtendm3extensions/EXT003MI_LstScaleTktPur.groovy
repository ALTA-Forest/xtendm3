// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-08-10
// @version   1.0 
//
// Description 
// This API is to list scale tickets from EXTDSP
// Transaction LstScaleTktPur
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: CTNO - Contract Number
 * @param: SUNO - Supplier
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: STID - Scale Ticket ID
 * @return: DLNO - Delivery Number
 * @return: CTNO - Contract Number
 * @return: TREF - Reference
 * @return: FACI - Facility
 * @return: DLDT - Delivery Date
 * @return: SUNO - Supplier
 * @return: YARD - Yard
 * @return: MSGN - Message Number
 * @return: INBN - Invoice Batch Number
 * 
*/

public class LstScaleTktPur extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inDLNO
  int inCTNO
  String inSUNO
  int numberOfFields
  
  // Constructor 
  public LstScaleTktPur(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
     this.mi = mi
     this.database = database 
     this.program = program
  } 
    
  public void main() { 
     // Set Company Number
     inCONO = mi.in.get("CONO")      
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     if (mi.in.get("DIVI") != null && mi.in.get("DIVI") != "") {
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

     // Supplier
     if (mi.inData.get("SUNO") != null) {
        inSUNO = mi.inData.get("SUNO").trim() 
     } else {
        inSUNO = ""      
     }


     // List scale tickets from EXTDST
     listScaleTickets()
  }
 
  //******************************************************************** 
  // List Scale Tickets from LstScaleTicket
  //******************************************************************** 
  void listScaleTickets(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTDSP")

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

     if (inSUNO != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXSUNO", inSUNO))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXSUNO", inSUNO)
         numberOfFields = 1
       }
     }


     DBAction actionline = database.table("EXTDSP").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   

     line.set("EXCONO", inCONO)
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()                 
     actionline.readAll(line, 1, pageSize, releasedLineProcessor)               
   } 

    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("STID", line.get("EXSTID").toString())
      mi.outData.put("DLNO", line.get("EXDLNO").toString())
      mi.outData.put("CTNO", line.get("EXCTNO").toString())
      mi.outData.put("TREF", line.getString("EXTREF"))
      mi.outData.put("FACI", line.get("EXFACI").toString())
      mi.outData.put("DLDT", line.get("EXDLDT").toString())
      mi.outData.put("SUNO", line.getString("EXSUNO"))
      mi.outData.put("YARD", line.getString("EXYARD"))
      mi.outData.put("MSGN", line.getString("EXMSGN"))
      mi.outData.put("INBN", line.get("EXINBN").toString())
      mi.outData.put("SPEC", line.getString("EXSPEC"))
      mi.outData.put("LOGS", line.get("EXLOGS").toString())
      mi.outData.put("AMNT", line.get("EXAMNT").toString())
      mi.write() 
   } 
   
}
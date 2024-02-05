// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list scale tickets from EXTDST
// Transaction LstScaleTicket
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: STLR - Log Rule
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DLNO - Delivery Number
 * @return: STNO - Scale Ticket Number
 * @return: STDT - Scale Date
 * @return: STLR - Log Rule
 * @return: STLN - Scale Location Number
 * @return: STSN - Scaler Number
 * @return: STID - Scale Ticket ID
 * @return: STLP - Log Percentage

 * 
*/

public class LstScaleTicket extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inDLNO
  int inSTLR
  int numberOfFields
  
  // Constructor 
  public LstScaleTicket(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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

     // Log Rule
     if (mi.in.get("STLR") != null) {
        inSTLR = mi.in.get("STLR") 
     } else {
        inSTLR = 0      
     }


     // List scale tickets from EXTDST
     listScaleTickets()
  }
 
  //******************************************************************** 
  // List Scale Tickets from LstScaleTicket
  //******************************************************************** 
  void listScaleTickets(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTDST")

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

     if (inSTLR != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXSTLR", String.valueOf(inSTLR)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXSTLR", String.valueOf(inSTLR))
         numberOfFields = 1
       }
     }


     DBAction actionline = database.table("EXTDST").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()                 
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               

   } 

    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("DLNO", line.get("EXDLNO").toString())
      mi.outData.put("STLR", line.get("EXSTLR").toString())
      mi.outData.put("STNO", line.getString("EXSTNO"))
      mi.outData.put("STDT", line.get("EXSTDT").toString())
      mi.outData.put("STLR", line.get("EXSTLR").toString())
      mi.outData.put("STLN", line.getString("EXSTLN"))
      mi.outData.put("STSN", line.getString("EXSTSN"))
      mi.outData.put("STID", line.get("EXSTID").toString())
      mi.outData.put("STLP", line.get("EXSTLP").toString())
      mi.write() 
   } 
   
}
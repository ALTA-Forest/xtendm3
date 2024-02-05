// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list scale ticket lines from EXTDWT
// Transaction LstWeightTktLn
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: STID - Scale Ticket ID
 * @param: PONR - Line Number
 * @param: ITNO - Item Number
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: STID - Scale Ticket ID
 * @return: PONR - Line Number
 * @return: ITNO - Item Number
 * @return: ORQT - Quantity
 * @return: STAM - Share Amount
 * @return: STSN - Scaler Number
 * @return: STID - Scale Ticket ID
 * @return: STLP - Log Percentage

 * 
*/

public class LstWeightTktLn extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inWTNO
  int inDLNO
  int numberOfFields
  
  // Constructor 
  public LstWeightTktLn(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
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
    
     // Weight Ticket ID
     if (mi.in.get("WTNO") != null) {
        inWTNO = mi.in.get("WTNO") 
     } else {
        inWTNO = 0      
     }

     // Delivery Number
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0      
     }


     // List scale ticket lines from EXTDSL
     listWeightTicketLines()
  }
 
  //******************************************************************** 
  // List Weight Ticket Lines from EXTDWT
  //******************************************************************** 
  void listWeightTicketLines(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTDWT")

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

     if (inWTNO != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXWTNO", String.valueOf(inWTNO)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXWTNO", String.valueOf(inWTNO))
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


     DBAction actionline = database.table("EXTDWT").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()         
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               
   } 

    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("WTNO", line.get("EXWTNO").toString())
      mi.outData.put("WTKN", line.getString("EXWTKN"))
      mi.outData.put("WTDT", line.get("EXWTDT").toString())
      mi.outData.put("WTLN", line.getString("EXWTLN"))
      mi.outData.put("DLNO", line.get("EXDLNO").toString())
      mi.outData.put("GRWE", line.get("EXGRWE").toString())
      mi.outData.put("TRWE", line.get("EXTRWE").toString())
      mi.outData.put("NEWE", line.get("EXNEWE").toString())
      mi.write() 
   } 
   
}
// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list scale ticket lines from EXTDSL
// Transaction LstScaleTktLn
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
 * @return: EOQT - Gross Quantity
 * @return: ORQT - Quantity
 * @return: STAM - Share Amount
 * @return: STSN - Scaler Number
 * @return: STID - Scale Ticket ID
 * @return: STLP - Log Percentage
 * 
*/

public class LstScaleTktLn extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inSTID
  int inPONR
  String inITNO
  int numberOfFields
  
  // Constructor 
  public LstScaleTktLn(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
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
    
     // Scale Ticket ID
     if (mi.in.get("STID") != null) {
        inSTID = mi.in.get("STID") 
     } else {
        inSTID = 0      
     }

     // Line Number
     if (mi.in.get("PONR") != null) {
        inPONR = mi.in.get("PONR") 
     } else {
        inPONR = 0      
     }

     // Item Number
     if (mi.in.get("ITNO") != null && mi.in.get("ITNO") != "") {
        inITNO = mi.inData.get("ITNO").trim() 
     } else {
        inITNO = ""     
     }


     // List scale ticket lines from EXTDSL
     listScaleTicketLines()
  }
 
  //******************************************************************** 
  // List Scale Ticket Lines from EXTDSL
  //******************************************************************** 
  void listScaleTicketLines(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTDSL")

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

     if (inSTID != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXSTID", String.valueOf(inSTID)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXSTID", String.valueOf(inSTID))
         numberOfFields = 1
       }
     }

     if (inPONR != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXPONR", String.valueOf(inPONR)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXPONR", String.valueOf(inPONR))
         numberOfFields = 1
       }
     }

     if (inITNO != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXITNO", inITNO))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXITNO", inITNO)
         numberOfFields = 1
       }
     }


     DBAction actionline = database.table("EXTDSL").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   

     line.set("EXCONO", inCONO)
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()             
     actionline.readAll(line, 1, pageSize, releasedLineProcessor)               
   } 

    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("STID", line.get("EXSTID").toString())
      mi.outData.put("PONR", line.get("EXPONR").toString())
      mi.outData.put("ITNO", line.getString("EXITNO"))
      mi.outData.put("EOQT", line.get("EXEOQT").toString())
      mi.outData.put("ORQT", line.get("EXORQT").toString())
      mi.outData.put("STAM", line.get("EXSTAM").toString())
      mi.write() 
   } 
   
}
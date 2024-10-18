// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list deck types from EXTDPT
// Transaction LstDeck
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

// Date         Changed By                         Description
// 2023-04-10   Jessica Bjorklund (Columbus)       Creation
// 2024-10-17   Jessica Bjorklund (Columbus)       Allow blank input for Strings

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: TYPE - Deck Type
 * @param: NAME - Name
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: TYPE - Deck Type
 * @return: NAME - Name
 * 
*/


public class LstDeckType extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  String inTYPE
  String inNAME
  int numberOfFields

  
  // Constructor 
  public LstDeckType(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
     if (mi.in.get("DIVI") != null) {
        inDIVI = mi.inData.get("DIVI").trim() 
     } else {
        inDIVI = ""     
     }
    
     // Deck Type
     if (mi.in.get("TYPE") != null) {
        inTYPE = mi.inData.get("TYPE").trim() 
     } else {
        inTYPE = ""     
     }
     
     // Name
     if (mi.in.get("NAME") != null) {
        inNAME = mi.inData.get("NAME").trim() 
     } else {
        inNAME = ""   
     }


     // List deck types
     listDeckTypes()
  }

  //******************************************************************** 
  // List Deck Types from EXTDPT
  //******************************************************************** 
  void listDeckTypes(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTDPT")

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

     if (inTYPE != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXTYPE", inTYPE))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXTYPE", inTYPE)
         numberOfFields = 1
       }
     }

     if (inNAME != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXNAME", inNAME))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXNAME", inNAME)
         numberOfFields = 1
       }
     }


     DBAction actionline = database.table("EXTDPT").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   

     line.set("EXCONO", inCONO)
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()                
     actionline.readAll(line, 1, pageSize, releasedLineProcessor)               
  }

    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("TYPE", line.getString("EXTYPE")) 
      mi.outData.put("NAME", line.getString("EXNAME")) 
      mi.write() 
   }
   
}
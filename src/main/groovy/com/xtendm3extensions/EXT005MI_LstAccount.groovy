// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list accounts from EXTACT
// Transaction LstAccount
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: ACCD - Account
 * @param: NAME - Name
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: ACCD - Account
 * @return: NAME - Name
 * 
*/

public class LstAccount extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  String inACCD
  String inNAME
  int numberOfFields

  
  // Constructor 
  public LstAccount(MIAPI mi, DatabaseAPI database,ProgramAPI program) {
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
    
     // Account
     if (mi.in.get("ACCD") != null) {
        inACCD = mi.inData.get("ACCD").trim() 
     } else {
        inACCD = ""     
     }
     
     // Name
     if (mi.in.get("NAME") != null) {
        inNAME = mi.inData.get("NAME").trim() 
     } else {
        inNAME = ""   
     }


     // List accounts
     listAccounts()
  }

  //******************************************************************** 
  // List Accounts from EXTACT
  //******************************************************************** 
  void listAccounts(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTACT")

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

     if (inACCD != "") {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXACCD", inACCD))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXACCD", inACCD)
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

     DBAction actionline = database.table("EXTACT").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   

     line.set("EXCONO", inCONO)
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()             
     actionline.readAll(line, 1, pageSize, releasedLineProcessor)               
  }


    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("ACCD", line.getString("EXACCD")) 
      mi.outData.put("NAME", line.getString("EXNAME")) 
      mi.write() 
   } 
   
}
// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list log headers from EXTSLH
// Transaction LstLogHeader
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

// Date         Changed By                         Description
// 2023-05-10   Jessica Bjorklund (Columbus)       Creation
// 2024-07-19   Jessica Bjorklund (Columbus)       Add DSID and SRID as output fields

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: STID - Scale Ticket ID
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: STID - Scale Ticket ID
 * @return: SEQN - Log Number
 * @return: TDCK - To Deck
 * @return: SPEC - Species
 * @return: ECOD - Exception Code
 * @return: TGNO - Tag Number
 * @return: LGID - Log ID
 * @return: LAMT - Amount
 * @return: DSID - Section ID
 * @return: SRID - Rate ID
 * 
*/

public class LstLogHeader extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inSTID
  int numberOfFields
  
  // Constructor 
  public LstLogHeader(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
        inDIVI = mi.in.get("DIVI") 
     } else {
        inDIVI = ""     
     }
    
     // Scale Ticket ID
     if (mi.in.get("STID") != null) {
        inSTID = mi.in.get("STID") 
     } else {
        inSTID = 0      
     }


     // List scale tickets from EXTSLH
     listLogHeaders()
  }
 
  //******************************************************************** 
  // List Log Header f
  //******************************************************************** 
  void listLogHeaders(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTSLH")

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


     DBAction actionline = database.table("EXTSLH").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   

     line.set("EXCONO", inCONO)
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()              
     actionline.readAll(line, 1, pageSize, releasedLineProcessor)               
   } 

    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("STID", line.get("EXSTID").toString())
      mi.outData.put("SEQN", line.get("EXSEQN").toString())
      mi.outData.put("TDCK", line.get("EXTDCK").toString())
      mi.outData.put("SPEC", line.getString("EXSPEC"))
      mi.outData.put("ECOD", line.getString("EXECOD"))
      mi.outData.put("TGNO", line.getString("EXTGNO"))
      mi.outData.put("LGID", line.get("EXLGID").toString())
      mi.outData.put("LAMT", line.get("EXLAMT").toString())
      mi.outData.put("DSID", line.get("EXDSID").toString())
      mi.outData.put("SRID", line.get("EXSRID").toString())
      mi.write() 
   } 
   
}
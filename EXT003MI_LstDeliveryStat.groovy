// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to list delivery status from EXTDLS
// Transaction LstDeliveryStat
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: SEQN - Sequence
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DLNO - Delivery Number
 * @return: SEQN - Sequence Number
 * @return: STAT - Status
 * @return: MDUL - Module
 * @return: CRDT - Date Updated
 * @return: USID - Updated By
 * @return: NOTE - Comment
 * 
*/

public class LstDeliveryStat extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inDLNO
  int inSEQN
  int numberOfFields
  
  // Constructor 
  public LstDeliveryStat(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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

     // Sequence
     if (mi.in.get("SEQN") != null) {
        inSEQN = mi.in.get("SEQN") 
     } else {
        inSEQN = 0      
     }


     // List delivery status lines from EXTDSL
     listDeliveryStatusLines()
  }
 
  //******************************************************************** 
  // List Delivery Status Lines from EXTDLS
  //******************************************************************** 
  void listDeliveryStatusLines(){ 
     ExpressionFactory expression = database.getExpressionFactory("EXTDLS")

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

     if (inSEQN != 0) {
       if (numberOfFields > 0) {
         expression = expression.and(expression.eq("EXSEQN", String.valueOf(inSEQN)))
         numberOfFields = 1
       } else {
         expression = expression.eq("EXSEQN", String.valueOf(inSEQN))
         numberOfFields = 1
       }
     }


     DBAction actionline = database.table("EXTDLS").index("00").matching(expression).selectAllFields().build()
	   DBContainer line = actionline.getContainer()   
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()                 
     actionline.readAll(line, 0, pageSize, releasedLineProcessor)               
   } 

    Closure<?> releasedLineProcessor = { DBContainer line -> 
      mi.outData.put("CONO", line.get("EXCONO").toString())
      mi.outData.put("DIVI", line.getString("EXDIVI"))
      mi.outData.put("DLNO", line.get("EXDLNO").toString())
      mi.outData.put("SEQN", line.get("EXSEQN").toString())
      mi.outData.put("STAT", line.get("EXSTAT").toString())
      mi.outData.put("MDUL", line.getString("EXMDUL"))
      mi.outData.put("CRDT", line.get("EXCRDT").toString())
      mi.outData.put("USID", line.getString("EXUSID"))
      mi.outData.put("NOTE", line.getString("EXNOTE"))
      mi.write() 
   }
   
}
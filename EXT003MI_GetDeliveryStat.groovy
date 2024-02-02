// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to get a delivery status record from EXTDLS
// Transaction GetDeliveryStat
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



public class GetDeliveryStat extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inDLNO
  int inSEQN
  
  // Constructor 
  public GetDeliveryStat(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
     inDIVI = mi.in.get("DIVI")
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
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

     // Get record
     getRecord()
  }
 
  //******************************************************************** 
  //Get EXTDLS record
  //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTDLS").index("00").selectAllFields().build()
     DBContainer EXTDLS = action.getContainer()
     EXTDLS.set("EXCONO", inCONO)
     EXTDLS.set("EXDIVI", inDIVI)
     EXTDLS.set("EXDLNO", inDLNO)
     EXTDLS.set("EXSEQN", inSEQN)
     
     // Read  
     if (action.read(EXTDLS)) {  
        mi.outData.put("CONO", EXTDLS.get("EXCONO").toString())
        mi.outData.put("DIVI", EXTDLS.getString("EXDIVI"))
        mi.outData.put("DLNO", EXTDLS.get("EXDLNO").toString())
        mi.outData.put("SEQN", EXTDLS.get("EXSEQN").toString())
        mi.outData.put("STAT", EXTDLS.get("EXSTAT").toString())
        mi.outData.put("MDUL", EXTDLS.getString("EXMDUL"))
        mi.outData.put("CRDT", EXTDLS.get("EXCRDT").toString())
        mi.outData.put("USID", EXTDLS.getString("EXUSID"))
        mi.outData.put("NOTE", EXTDLS.getString("EXNOTE"))      
        mi.write()  
     } else {
        mi.error("No record found")   
        return 
     }
  }
  
}
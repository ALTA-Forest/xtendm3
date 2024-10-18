// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to get a log header record from EXTSLH
// Transaction GetLogHeader
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
 * @param: SEQN - Log Number
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



public class GetLogHeader extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database 
  private final ProgramAPI program
  
  Integer inCONO
  String inDIVI
  int inSTID
  int inSEQN
  
  // Constructor 
  public GetLogHeader(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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

     // Scale Ticket ID
     if (mi.in.get("STID") != null) {
        inSTID = mi.in.get("STID") 
     } else {
        inSTID = 0         
     }

     // Log Number
     if (mi.in.get("SEQN") != null) {
        inSEQN = mi.in.get("SEQN") 
     } else {
        inSEQN = 0         
     }

     // Get record
     getRecord()
  }
 
 //******************************************************************** 
 //Get EXTSLH record
 //********************************************************************     
  void getRecord(){      
     DBAction action = database.table("EXTSLH").index("00").selectAllFields().build()
     DBContainer EXTSLH = action.getContainer()
     EXTSLH.set("EXCONO", inCONO)
     EXTSLH.set("EXDIVI", inDIVI)
     EXTSLH.set("EXSTID", inSTID)
     EXTSLH.set("EXSEQN", inSEQN)
     
     // Send output value 
     if (action.read(EXTSLH)) { 
        mi.outData.put("CONO", EXTSLH.get("EXCONO").toString())
        mi.outData.put("DIVI", EXTSLH.getString("EXDIVI"))
        mi.outData.put("STID", EXTSLH.get("EXSTID").toString())
        mi.outData.put("SEQN", EXTSLH.get("EXSEQN").toString())
        mi.outData.put("TDCK", EXTSLH.get("EXTDCK").toString())
        mi.outData.put("SPEC", EXTSLH.getString("EXSPEC"))
        mi.outData.put("ECOD", EXTSLH.getString("EXECOD"))
        mi.outData.put("TGNO", EXTSLH.getString("EXTGNO"))
        mi.outData.put("LGID", EXTSLH.get("EXLGID").toString())
        mi.outData.put("LAMT", EXTSLH.get("EXLAMT").toString()) 
        mi.outData.put("DSID", EXTSLH.get("EXDSID").toString())
        mi.outData.put("SRID", EXTSLH.get("EXSRID").toString())
        mi.write()
     } else {
        mi.error("No record found")   
        return 
     }
  }
  
}
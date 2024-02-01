// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API will add a contract template to EXTCTH and EXTCTD
// Transaction AddContrTemp
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: CTNO - Contract Number to copy from
 * @param: RVID - Revision ID to copy from
 * 
*/

/**
 * OUT
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: CTNO - New Contract Number
 * @param: RVID - New Revision ID
 * 
*/


public class AddContrTemp extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  // Definition 
  Integer inCONO
  String inDIVI
  String inRVID
  int inCTNO
  int outCTNO 
  int outRVNO
  String outRVID
  String nextNumber
  int oldCTYP
  String oldCTMG
  String oldDLTY 
  int oldCFI5
  String inCTTI
  int inSTAT
  String inPTPC
  String inRTPC
  String inTEPY
  int inFRSC
  String inCMNO
  int inVALF
  int inVALT
  String revisionID
  int sectionID
  String sectionName
  int sectionGrade
  int sectionException
  int displayOrder
  int lengthIncrement
  int species
  int newSectionID
  String newSectionIDString

  
  // Constructor 
  public AddContrTemp(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.logger = logger
     this.utility = utility
  } 
    
  public void main() {       
     // Set Company Number
     inCONO = mi.in.get("CONO")      
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     inDIVI = mi.inData.get("DIVI").trim()
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }

     // Contract Number to copy from
     if (mi.in.get("CTNO") != null) {
        inCTNO = mi.in.get("CTNO") 
     } else {
        inCTNO = 0          
     }
      
     // Revision Number to copy from
     if (mi.in.get("RVID") != null) {
        inRVID = mi.in.get("RVID") 
     } else {
        inRVID = ""        
     }
          
     // Revision Number
     outRVNO = 1
     
     //Get next number for contract number
     getNextNumber("", "L1", "1") 
     outCTNO = nextNumber as Integer

     //Revision ID
     String outRVID = String.valueOf(outCTNO) + String.valueOf(outRVNO)

     // Validate contract header
     Optional<DBContainer> EXTCTH = findEXTCTH(inCONO, inDIVI, inCTNO)
     if (EXTCTH.isPresent()) {
        // Record found, get header information from contract to be copied to new contract template 
        DBContainer containerEXTCTH = EXTCTH.get() 
        oldCTYP = containerEXTCTH.get("EXCTYP") 
        oldCTMG = containerEXTCTH.getString("EXCTMG") 
        oldDLTY = containerEXTCTH.getString("EXDLTY") 
        oldCFI5 = containerEXTCTH.get("EXCFI5") 
     } else {
        mi.error("Contract Number doesn't exist in Contract Header table")   
        return  
     }

     // Get contract detail record from entered revision
     Optional<DBContainer> EXTCTD = findEXTCTD(inCONO, inDIVI, inRVID)
     if (EXTCTD.isPresent()) {
       // Record found, get detail information from contract  
       DBContainer containerEXTCTD = EXTCTD.get() 
       inPTPC = containerEXTCTD.getString("EXPTPC") 
       inRTPC = containerEXTCTD.getString("EXRTPC") 
       inTEPY = containerEXTCTD.getString("EXTEPY") 
       inFRSC = containerEXTCTD.get("EXFRSC") 
       inCMNO = containerEXTCTD.getString("EXCMNO") 
       inVALF = containerEXTCTD.get("EXVALF") 
       inVALT = containerEXTCTD.get("EXVALT") 
     } else {
       mi.error("Contract Number/Revision ID doesn't exist in Contract Details table")   
       return             
     }

     //Copy Instructions for the revisions
     List<DBContainer> resultEXTCTI = listEXTCTI(inCONO, inDIVI, inRVID) 
     for (DBContainer recLineEXTCTI : resultEXTCTI){ 
       String revisionInstructionCodeString = recLineEXTCTI.getString("EXINIC")
       String displayOrderString = recLineEXTCTI.get("EXDPOR")

       addInstructionSpecMI(String.valueOf(inCONO), inDIVI, outRVID, revisionInstructionCodeString, displayOrderString)   
     }

     // Write record 
     addEXTCTHRecord(inCONO, inDIVI, outCTNO, oldCTYP, oldCTMG, oldDLTY, oldCFI5, "Template", outRVID)  
     addEXTCTDRecord(inCONO, inDIVI, outCTNO, inPTPC, inRTPC, inTEPY, inFRSC, inCMNO, inVALF, inVALT, outRVID)    
     
     //Copy Selections
     List<DBContainer> resultEXTCDS = listEXTCDS(inCONO, inDIVI, inRVID) 
     for (DBContainer recLineEXTCDS : resultEXTCDS){ 
        String revisionIDString = recLineEXTCDS.get("EXRVID")
        String sectionIDString = recLineEXTCDS.get("EXDSID")
        int sectionID = recLineEXTCDS.get("EXDSID")
        String sectionNumberString = recLineEXTCDS.get("EXCSSN")
        int sectionNumber = recLineEXTCDS.get("EXCSSN")
        String sectionNameString = recLineEXTCDS.get("EXCSNA")
        String displayOrderString = recLineEXTCDS.get("EXDPOR")
        String speciesString = recLineEXTCDS.get("EXCSSP")
        String sectionGradeString = recLineEXTCDS.get("EXCSGR")
        String sectionExceptionString = recLineEXTCDS.get("EXCSEX")
        String lengthIncrementString = recLineEXTCDS.get("EXCSLI")

        addContractSectionMI(String.valueOf(inCONO), inDIVI, outRVID, sectionNumberString, sectionNameString, displayOrderString, speciesString, sectionGradeString, sectionExceptionString, lengthIncrementString)   

        //Copy Species for each section
        List<DBContainer> resultEXTCSS = listEXTCSS(inCONO, inDIVI, sectionID) 
        for (DBContainer recLineEXTCSS : resultEXTCSS){ 
          String sectionIDSpeciesString = recLineEXTCSS.get("EXDSID")
          String speciesNameString = recLineEXTCSS.get("EXSPEC")

          addSectionSpecMI(String.valueOf(inCONO), inDIVI, newSectionIDString, speciesNameString)   
        }

        //Copy Sections Grades for each section
        List<DBContainer> resultEXTCSG = listEXTCSG(inCONO, inDIVI, sectionID) 
        for (DBContainer recLineEXTCSG : resultEXTCSG){ 
          String sectionIDGradesString = recLineEXTCSG.get("EXDSID")
          String gradeCodeString = recLineEXTCSG.get("EXGRAD")

          addSectionGradeMI(String.valueOf(inCONO), inDIVI, newSectionIDString, gradeCodeString)   
        }
 
        //Copy Sections Exceptions for each section
        List<DBContainer> resultEXTCSE = listEXTCSE(inCONO, inDIVI, sectionID) 
        for (DBContainer recLineEXTCSE : resultEXTCSE){ 
          String sectionIDExceptionString = recLineEXTCSE.get("EXDSID")
          String exceptionCodeString = recLineEXTCSE.get("EXECOD")

          addSectionExeptionMI(String.valueOf(inCONO), inDIVI, newSectionIDString, exceptionCodeString)   
        }

        //Copy Sections Rate for each section
        List<DBContainer> resultEXTCSR = listEXTCSR(inCONO, inDIVI, sectionID) 
        for (DBContainer recLineEXTCSR : resultEXTCSR){ 
          String sectionIDRateString = recLineEXTCSR.get("EXDSID")
          String rateSeqString = recLineEXTCSR.get("EXCRSQ")
          String minLengthString = recLineEXTCSR.get("EXCRML")
          String maxLengthString = recLineEXTCSR.get("EXCRXL")
          String minDiamString = recLineEXTCSR.get("EXCRMD")
          String maxDiamString = recLineEXTCSR.get("EXCRXD")
          String amountString = recLineEXTCSR.get("EXCRRA")
          String noteString = recLineEXTCSR.get("EXCRNO")

          addSectionRateMI(String.valueOf(inCONO), inDIVI, newSectionIDString, rateSeqString, minLengthString, maxLengthString, minDiamString, maxDiamString, amountString, noteString)   
        }
        
     }

     mi.outData.put("CONO", String.valueOf(inCONO)) 
     mi.outData.put("DIVI", inDIVI) 
     mi.outData.put("CTNO", String.valueOf(outCTNO)) 
     mi.outData.put("RVID", outRVID) 
     mi.write()
  }
  
    
  //******************************************************************** 
  // Get EXTCTH record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCTH(int CONO, String DIVI, int CTNO){  
     DBAction query = database.table("EXTCTH").index("00").selection("EXCTYP", "EXCTMG", "EXDLTY", "EXCFI5").build()
     DBContainer EXTCTH = query.getContainer()
     EXTCTH.set("EXCONO", CONO)
     EXTCTH.set("EXDIVI", DIVI)
     EXTCTH.set("EXCTNO", CTNO)
     if(query.read(EXTCTH))  { 
       return Optional.of(EXTCTH)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Get EXTCTD record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCTD(int CONO, String DIVI, String RVID){  
     DBAction query = database.table("EXTCTD").index("00").selection("EXPTPC", "EXRTPC", "EXTEPY", "EXFRSC", "EXCMNO", "EXVALF", "EXVALT").build()
     DBContainer EXTCTD = query.getContainer()
     EXTCTD.set("EXCONO", CONO)
     EXTCTD.set("EXDIVI", DIVI)
     EXTCTD.set("EXRVID", RVID)
     if(query.read(EXTCTD))  { 
       return Optional.of(EXTCTD)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Read records from instruction table EXTCTI
  //********************************************************************  
  private List<DBContainer> listEXTCTI(int CONO, String DIVI, String RVID){
    List<DBContainer>recLineEXTCTI = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTCTI")
    expression = expression.eq("EXCONO", String.valueOf(CONO))
    
    DBAction query = database.table("EXTCTI").index("00").matching(expression).selection("EXINIC", "EXDPOR").build()
    DBContainer EXTCTI = query.createContainer()
    EXTCTI.set("EXCONO", CONO)
    EXTCTI.set("EXDIVI", DIVI)
    EXTCTI.set("EXRVID", RVID)

    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
    query.readAll(EXTCTI, 3, pageSize, { DBContainer recordEXTCTI ->  
       recLineEXTCTI.add(recordEXTCTI.createCopy()) 
    })

    return recLineEXTCTI
  }
  
   //***************************************************************************** 
   // Get next number in the number serie using CRS165MI.RtvNextNumber    
   // Input 
   // Division
   // Number Series Type
   // Number Seriew
   //***************************************************************************** 
   void getNextNumber(String division, String numberSeriesType, String numberSeries){   
        Map<String, String> params = [DIVI: division, NBTY: numberSeriesType, NBID: numberSeries] 
        Closure<?> callback = {
        Map<String, String> response ->
          if(response.NBNR != null){
            nextNumber = response.NBNR
          }
        }

        miCaller.call("CRS165MI","RtvNextNumber", params, callback)
   } 


   //***************************************************************************** 
   // Add Contract Section
   //***************************************************************************** 
   void addContractSectionMI(String company, String division, String revisionID, String sectionID, String sectionName, String displayOrder, String species, String sectionGrade, String sectionException, String lengthIncrement){   
        Map<String, String> params = [CONO: company, DIVI: division, RVID: revisionID, CSSN: sectionID, CSNA: sectionName, DPOR: displayOrder, CSSP: species, CSGR: sectionGrade, CSEX: sectionException, CSLI: lengthIncrement] 
        Closure<?> callback = {
          Map<String, String> response ->
          if(response.DSID != null){
            newSectionIDString = response.DSID 
            newSectionID = newSectionIDString as Integer
          }
        }
        
        miCaller.call("EXT002MI","AddContrSection", params, callback)
   } 

   //***************************************************************************** 
   // Add Section Species
   //***************************************************************************** 
   void addSectionSpecMI(String company, String division, String sectionID, String speciesName){   
        Map<String, String> params = [CONO: company, DIVI: division, DSID: sectionID, SPEC: speciesName] 
        Closure<?> callback = {
          Map<String, String> response ->
          if(response.SSID != null){
          }
        }
        
        miCaller.call("EXT002MI","AddSectionSpec", params, callback)
   } 

   //***************************************************************************** 
   // Add Section Grade
   //***************************************************************************** 
   void addSectionGradeMI(String company, String division, String sectionID, String gradeCode){   
        Map<String, String> params = [CONO: company, DIVI: division, DSID: sectionID, GRAD: gradeCode] 
        Closure<?> callback = {
          Map<String, String> response ->
          if(response.DSID != null){
          }
        }

        miCaller.call("EXT002MI","AddSectionGrade", params, callback)
   } 

   //***************************************************************************** 
   // Add Section Exception
   //***************************************************************************** 
   void addSectionExeptionMI(String company, String division, String sectionID, String exceptionCode){   
        Map<String, String> params = [CONO: company, DIVI: division, DSID: sectionID, ECOD: exceptionCode] 
        Closure<?> callback = {
          Map<String, String> response ->
          if(response.DSID != null){
          }
        }

        miCaller.call("EXT002MI","AddSectionExcep", params, callback)
   } 

   //***************************************************************************** 
   // Add Instruction to the revision
   //***************************************************************************** 
   void addInstructionSpecMI(String company, String division, String revisionID, String instructionCode, String displayOrder){   
        Map<String, String> params = [CONO: company, DIVI: division, RVID: revisionID, INIC: instructionCode, DPOR: displayOrder] 
        Closure<?> callback = {
          Map<String, String> response ->
          if(response.CIID != null){
          }
        }
        
        miCaller.call("EXT002MI","AddContrInst", params, callback)
   } 

   //***************************************************************************** 
   // Add Section Rate
   //***************************************************************************** 
   void addSectionRateMI(String company, String division, String sectionID, String rateSeq, String minLength, String maxLength, String minDiam, String maxDiam, String amount, String note){   
        Map<String, String> params = [CONO: company, DIVI: division, DSID: sectionID, CRSQ: rateSeq, CRML: minLength, CRXL: maxLength, CRMD: minDiam, CRXD: maxDiam, CRRA: amount, CRNO: note] 
        Closure<?> callback = {
          Map<String, String> response ->
          if(response.DSID != null){
          }
        }

        miCaller.call("EXT002MI","AddSectionRate", params, callback)
   } 
   

  //******************************************************************** 
  // Add EXTCTH record
  //********************************************************************     
  void addEXTCTHRecord(int CONO, String DIVI, int CTNO, int CTYP, String CTMG, String DLTY, int CFI5, String CTTI, String RVID){     
       DBAction action = database.table("EXTCTH").index("00").build()
       DBContainer EXTCTH = action.createContainer()
       EXTCTH.set("EXCONO", CONO)
       EXTCTH.set("EXDIVI", DIVI)
       EXTCTH.set("EXCTNO", CTNO)
       EXTCTH.set("EXCTYP", CTYP)
       EXTCTH.set("EXCTMG", CTMG)
       EXTCTH.set("EXDLTY", DLTY)
       EXTCTH.set("EXISTP", 1)
       EXTCTH.set("EXCFI5", CFI5)
       EXTCTH.set("EXCTTI", CTTI)
       EXTCTH.set("EXSTAT", 10)
       EXTCTH.set("EXRVID", RVID)
       EXTCTH.set("EXRVNO", 1)
       EXTCTH.set("EXCHID", program.getUser())
       EXTCTH.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTCTH.set("EXRGDT", regdate) 
       EXTCTH.set("EXLMDT", regdate) 
       EXTCTH.set("EXRGTM", regtime)
       action.insert(EXTCTH)         
 } 
 
 
  //******************************************************************** 
  // Add EXTCTD record 
  //********************************************************************     
  void addEXTCTDRecord(int CONO, String DIVI, int CTNO, String PTPC, String RTPC, String TEPY, int FRSC, String CMNO, int VALF, int VALT, String RVID){     
       DBAction action = database.table("EXTCTD").index("00").build()
       DBContainer EXTCTD = action.createContainer()
       EXTCTD.set("EXCONO", CONO)
       EXTCTD.set("EXDIVI", DIVI)
       EXTCTD.set("EXCTNO", CTNO)
       EXTCTD.set("EXPTPC", PTPC)
       EXTCTD.set("EXRTPC", RTPC)
       EXTCTD.set("EXTEPY", TEPY)
       EXTCTD.set("EXFRSC", FRSC)
       EXTCTD.set("EXCMNO", CMNO)
       EXTCTD.set("EXRVNO", 1)
       EXTCTD.set("EXRVID", RVID)
       EXTCTD.set("EXVALF", VALF)
       EXTCTD.set("EXVALT", VALT)
       EXTCTD.set("EXSTAT", 10)
       EXTCTD.set("EXCHID", program.getUser())
       EXTCTD.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTCTD.set("EXRGDT", regdate) 
       EXTCTD.set("EXLMDT", regdate) 
       EXTCTD.set("EXRGTM", regtime)
       action.insert(EXTCTD)         
 } 
     


 
  //******************************************************************** 
  // Add EXTCDS record - Contract Section
  //********************************************************************     
  void addContractSection(int CONO, String DIVI, String RVID, int CSSN, String CSNA, int DPOR, int CSSP, int CSGR, int CSEX, int CSLI){   
       DBAction action = database.table("EXTCDS").index("00").build()
       DBContainer EXTCDS = action.createContainer()
       EXTCDS.set("EXCONO", CONO)
       EXTCDS.set("EXDIVI", DIVI)
       EXTCDS.set("EXRVID", RVID)
       EXTCDS.set("EXCSSN", CSSN)
       EXTCDS.set("EXCSNA", CSNA)
       EXTCDS.set("EXDPOR", DPOR)
       EXTCDS.set("EXCSSP", CSSP)
       EXTCDS.set("EXCSGR", CSGR)
       EXTCDS.set("EXCSEX", CSEX)
       EXTCDS.set("EXCSLI", CSLI)
       EXTCDS.set("EXCHID", program.getUser())
       EXTCDS.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTCDS.set("EXRGDT", regdate) 
       EXTCDS.set("EXLMDT", regdate) 
       EXTCDS.set("EXRGTM", regtime)
       action.insert(EXTCDS)         
  } 
 

  //******************************************************************** 
  // Read records from section table EXTCDS
  //********************************************************************  
  private List<DBContainer> listEXTCDS(int CONO, String DIVI, String RVID){
    List<DBContainer>recLineEXTCDS = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTCDS")
    expression = expression.eq("EXCONO", String.valueOf(CONO))
    
    DBAction query = database.table("EXTCDS").index("00").matching(expression).selection("EXRVID", "EXDSID", "EXCSSN", "EXCSNA", "EXDPOR", "EXCSSP", "EXCSGR", "EXCSEX", "EXCSLI").build()
    DBContainer EXTCDS = query.createContainer()
    EXTCDS.set("EXCONO", CONO)
    EXTCDS.set("EXDIVI", DIVI)
    EXTCDS.set("EXRVID", RVID)
    
    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
    query.readAll(EXTCDS, 3, pageSize, { DBContainer recordEXTCDS ->  
       recLineEXTCDS.add(recordEXTCDS.createCopy()) 
    })

    return recLineEXTCDS
  }


  //******************************************************************** 
  // Read records from species table EXTCSS
  //********************************************************************  
  private List<DBContainer> listEXTCSS(int CONO, String DIVI, int DSID){
    List<DBContainer>recLineEXTCSS = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTCSS")
    expression = expression.eq("EXCONO", String.valueOf(CONO))
    
    DBAction query = database.table("EXTCSS").index("00").matching(expression).selection("EXDSID", "EXSPEC").build()
    DBContainer EXTCSS = query.createContainer()
    EXTCSS.set("EXCONO", CONO)
    EXTCSS.set("EXDIVI", DIVI)
    EXTCSS.set("EXDSID", DSID)
    
    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
    query.readAll(EXTCSS, 3, pageSize, { DBContainer recordEXTCSS ->  
       recLineEXTCSS.add(recordEXTCSS.createCopy()) 
    })

    return recLineEXTCSS
  }


  //******************************************************************** 
  // Read records from section grade table EXTCSG
  //********************************************************************  
  private List<DBContainer> listEXTCSG(int CONO, String DIVI, int DSID){
    List<DBContainer>recLineEXTCSG = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTCSG")
    expression = expression.eq("EXCONO", String.valueOf(CONO))
    
    DBAction query = database.table("EXTCSG").index("00").matching(expression).selection("EXDSID", "EXGRAD").build()
    DBContainer EXTCSG = query.createContainer()
    EXTCSG.set("EXCONO", CONO)
    EXTCSG.set("EXDIVI", DIVI)
    EXTCSG.set("EXDSID", DSID)

    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
    query.readAll(EXTCSG, 3, pageSize, { DBContainer recordEXTCSG ->  
       recLineEXTCSG.add(recordEXTCSG.createCopy()) 
    })

    return recLineEXTCSG
  }

  //******************************************************************** 
  // Read records from section exception table EXTCSE
  //********************************************************************  
  private List<DBContainer> listEXTCSE(int CONO, String DIVI, int DSID){
    List<DBContainer>recLineEXTCSE = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTCSE")
    expression = expression.eq("EXCONO", String.valueOf(CONO))
    
    DBAction query = database.table("EXTCSE").index("00").matching(expression).selection("EXDSID", "EXECOD").build()
    DBContainer EXTCSE = query.createContainer()
    EXTCSE.set("EXCONO", CONO)
    EXTCSE.set("EXDIVI", DIVI)
    EXTCSE.set("EXDSID", DSID)

    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
    query.readAll(EXTCSE, 3, pageSize, { DBContainer recordEXTCSE ->  
       recLineEXTCSE.add(recordEXTCSE.createCopy()) 
    })

    return recLineEXTCSE
  }

  //******************************************************************** 
  // Read records from section rate table EXTCSR
  //********************************************************************  
  private List<DBContainer> listEXTCSR(int CONO, String DIVI, int DSID){
    List<DBContainer>recLineEXTCSR = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTCSR")
    expression = expression.eq("EXCONO", String.valueOf(CONO))
    
    DBAction query = database.table("EXTCSR").index("00").matching(expression).selection("EXDSID", "EXCRSQ", "EXCRML", "EXCRXL", "EXCRMD", "EXCRXD", "EXCRRA", "EXCRNO").build()
    DBContainer EXTCSR = query.createContainer()
    EXTCSR.set("EXCONO", CONO)
    EXTCSR.set("EXDIVI", DIVI)
    EXTCSR.set("EXDSID", DSID)

    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
    query.readAll(EXTCSR, 3, pageSize, { DBContainer recordEXTCSR ->  
       recLineEXTCSR.add(recordEXTCSR.createCopy()) 
    })

    return recLineEXTCSR
  }

} 


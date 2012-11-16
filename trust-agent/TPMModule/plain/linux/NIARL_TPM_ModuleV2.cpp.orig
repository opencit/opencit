/**
 * Copyright (C) 2012, U.S. Government, National Security Agency, National Information Assurance Research Laboratory
 * This is a work of the UNITED STATES GOVERNMENT and is not subject to copyright protection in the United States. Foreign copyrights may apply.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2) Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3) Neither the name of the NATIONAL SECURITY AGENCY/NATIONAL INFORMATION ASSURANCE RESEARCH LABORATORY nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include "NIARL_TPM_ModuleV2.h"

NIARL_TPM_ModuleV2::NIARL_TPM_ModuleV2(int argc, char* argv[])
{
	//set defaults
	b_debug = false;
	b_log = false;
	b_help = false;
	b_infile = false;
	b_outfile = false;
	i_mode = 0;
	i_return = 0;

	//setup local copy of argument array
	i_argc = argc;
	s_argv = new string[i_argc];

	for(short i = 0; i < i_argc; i++)
	{
		if(strcmp(argv[i], "-debug") == 0)
		{
			b_debug = true;
			continue;
		}

		if(strcmp(argv[i], "-log") == 0)
		{
			b_log = true;
			continue;
		}

		if(strcmp(argv[i], "-help") == 0)
		{
			b_help = true;
			continue;
		}

		if(strcmp(argv[i], "-mode") == 0)
		{
			if(++i >= i_argc) return;
			i_mode = atoi(argv[i]);
			continue;
		}

/*		if(strcmp(argv[i], "-infile") == 0)
		{
			b_infile = true;
			infile.open(s_argv[(++i)].c_str(), ios::in);
			if(infile.is_open())
				cin.rdbuf(infile.rdbuf());
			else
				return_code = -1 * ERROR_ARG_INFILE;
			continue;
		}
*/
		if(strcmp(argv[i], "-outfile") == 0)
		{
			b_outfile = true;
			if(++i >= i_argc) return;
			outfile.open(s_argv[(i)].c_str(), ios::out);
			if(outfile.is_open())
				cout.rdbuf(outfile.rdbuf());
			else
				return_code = -1 * ERROR_ARG_OUTFILE;
			continue;
		}

		//convert function-specific c-string data to c++ strings
		s_argv[i] = argv[i];
	}

	if(b_debug || b_log)
	{
		//record the start time and output appropriate messages
		time_t rawtime;
		struct tm* timeinfo;
		time(&rawtime);
		timeinfo = localtime(&rawtime);

		if(b_debug)
		{
			cerr << "START --- NIARL TPM Module (v2.5 11-24-2010) --- " << asctime(timeinfo);
			cerr << ' ' << i_mode << " mode selection" << endl;
			cerr << ' ' << b_debug << " debug toggle" << endl;
			cerr << ' ' << logfile.is_open() << " logging" << endl;
			cerr << ' ' << infile.is_open() << " input file" << endl;
			cerr << ' ' << outfile.is_open() << " output file" << endl;
		}

		if(b_log)
		{
			clog << "START --- NIARL TPM Module (v2.5 11-24-2010) --- " << asctime(timeinfo);
			clog << ' ' << i_mode << " mode selection" << endl;
			clog << ' ' << b_debug << " debug toggle" << endl;
			clog << ' ' << logfile.is_open() << " logging" << endl;
			clog << ' ' << infile.is_open() << " input file" << endl;
			clog << ' ' << outfile.is_open() << " output file" << endl;
		}
	}

	if(b_help && i_mode == 0)
	{
		cout << endl << "NIARL TPM MODULE (Version 2.5, Build Date 11-25-2010) PLAIN, SYM-FIX, SEG-FIX" << endl;
		cout << endl << "MODE LIST" << endl;
		cout << " 1  --- Take Ownership" << endl;
		cout << " 2  --- Clear Ownership" << endl;
		cout << " 3  --- Collate Identity Request" << endl;
		cout << " 4  --- Activate Identity" << endl;
		cout << " 5  --- Quote" << endl;
		cout << " 6  --- Create Revokable Endorsement Key" << endl;
		cout << " 7  --- Revoke Revokable Endorsement Key" << endl;
		cout << " 8  --- Create Key (sign or bind)" << endl;
		cout << " 9  --- Set Key (sign, bind, or identity)" << endl;
		cout << " 10 --- Get Key (sign, bind, identity, or EK)" << endl;
		cout << " 11 --- Clear Key (sign, bind, or identity)" << endl;
		cout << " 12 --- Set Credential (EC, PC, CC, and PCC)" << endl;
		cout << " 13 --- Get Credential (EC, PC, CC, and PCC)" << endl;
		cout << " 14 --- Clear Credential (EC, PC, CC, and PCC)" << endl;
		cout << " 15 --- Seal" << endl;
		cout << " 16 --- Unseal" << endl;
		cout << " 17 --- Bind" << endl;
		cout << " 18 --- Unbind" << endl;
		cout << " 19 --- Seal Bind" << endl;
		cout << " 20 --- Unseal Unbind" << endl;
		cout << " 21 --- Get Random Integer" << endl;
		cout << " 22 --- Sign" << endl;
		cout << " 23 --- Create Endorsement Key" << endl;
		cout << " 24 --- Quote2" << endl;

		cout << endl << "INPUT FLAGS" << endl;
		cout << " -mode integer (mode selection flag)" << endl;
		cout << " -debug (debugging output displayed to cerr)" << endl;
		cout << " -outfile name.txt (standard output redirected to file named)" << endl;

		cout << endl << "ERROR CODES" << endl;
		cout << " TSS errors are positive integers. TPM Module errors are negative integers." << endl;
		cout << " -" << ERROR_UNKNOWN << " --- Unspecified error" << endl;
		cout << " -" << ERROR_ARG_MISSING << " --- Argument missing" << endl;
		cout << " -" << ERROR_ARG_INFILE << " --- Invalid or inaccessible input file" << endl;
		cout << " -" << ERROR_ARG_OUTFILE << " --- Invalid or inaccessible output file" << endl;
		cout << " -" << ERROR_ARG_MODE << " --- Invalid mode selection" << endl;
		cout << " -" << ERROR_ARG_VALIDATION << " --- Argument validation error" << endl;
		cout << " -" << ERROR_ARG_HELP << " --- Help toggle detected" << endl;
		cout << " -" << ERROR_MODE_DISABLED << " --- Mode selection disabled" << endl;
	}
}

NIARL_TPM_ModuleV2::~NIARL_TPM_ModuleV2()
{
	//delete dynamic arrays
	delete [] s_argv;

	//close logfile
	if(logfile.is_open())
		logfile.close();

/*	//close input file
	if(infile.is_open())
		infile.close();
*/
	//close output file
	if(outfile.is_open())
		outfile.close();

	if(b_debug || b_log)
	{
		//record the end time and output appropriate messages
		time_t rawtime;
		struct tm* timeinfo;
		time(&rawtime);
		timeinfo = localtime(&rawtime);

		if(b_debug)
			cerr << "END --- NIARL TPM Module --- " << asctime(timeinfo);

		if(b_log)
			clog << "END --- NIARL TPM Module --- " << asctime(timeinfo);
	}
}

void NIARL_TPM_ModuleV2::run_mode()
{
	switch(i_mode)
	{
	case MODE_TAKE_OWNERSHIP:
		take_ownership();
		break;
	case MODE_CLEAR_OWNERSHIP:
		clear_ownership();
		break;
	case MODE_COLLATE_IDENTITY:
		collate_identity();
		break;
	case MODE_ACTIVATE_IDENTITY:
		activate_identity();
		break;
	case MODE_QUOTE:
		quote();
		break;
	case MODE_CREATE_REK:
		create_revokable_ek();
		break;
	case MODE_REVOKE_REK:
		revoke_ek();
		break;
	case MODE_CREATE_KEY:
		create_key();
		break;
	case MODE_SET_KEY:
		set_key();
		break;
	case MODE_GET_KEY:
		get_key();
		break;
	case MODE_CLEAR_KEY:
		clear_key();
		break;
	case MODE_SET_CREDENTIAL:
		set_credential();
		break;
	case MODE_GET_CREDENTIAL:
		get_credential();
		break;
	case MODE_CLEAR_CREDENTIAL:
		clear_credential();
		break;
	case MODE_SEAL:
		seal();
		break;
	case MODE_UNSEAL:
		unseal();
		break;
	case MODE_BIND:
		bind();
		break;
	case MODE_UNBIND:
		unbind();
		break;
	case MODE_SEAL_BIND:
		seal_bind();
		break;
	case MODE_UNSEAL_UNBIND:
		unseal_unbind();
		break;
	case MODE_GET_RAND:
		get_rand_int();
		break;
	case MODE_SIGN:
		sign();
		break;
	case MODE_CREATE_EK:
		create_ek();
		break;
	case MODE_QUOTE2:
		quote2();
		break;
	default:
		return_code = -1 * ERROR_ARG_MODE;
		return;
	}
}

/**********************************************************************************************
	 Take Ownership
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::take_ownership()
{
	if(b_help)
	{
		cout << "Take Ownership (" << i_mode << ") --- Takes ownership of the TPM and establishes a Storage Root Key (SRK)" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -owner_auth (hex blob, owner authorization)" << endl;
		cout << " -nonce (hex blob, anti-replay nonce)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	string				s_ownerauth;
	string				s_nonce;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-owner_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_ownerauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-nonce") == 0)
		{
			if(++i >= i_argc) return;
			s_nonce = s_argv[i];
			i_success++;
		}
	}
	if(i_success != 2)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	ownerauth(s_ownerauth);
	NIARL_Util_ByteBlob	nonce(s_nonce);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;
	UINT32				wks_size = sizeof(wks_blob);


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;
	TSS_HPOLICY		policy_tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_tpm);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Policy_SetSecret(policy_tpm, TSS_SECRET_MODE_PLAIN, ownerauth.size, ownerauth.blob);
		if(b_debug)	cerr << ' ' << result << " owner auth" << endl;
		if(b_log)	cerr << ' ' << result << " owner auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_tpm, tpm);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;


//SRK OPERATIONS (NOT SET YET)
	TSS_HKEY		srk;
	TSS_HPOLICY		policy_srk;
	TSS_UUID		uuid_srk = TSS_UUID_SRK;

		if(b_debug)	cerr << "SRK Section" << endl;
		if(b_log)	clog << "SRK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_TSP_SRK, &srk);
		if(b_debug)	cerr << ' ' << result << " create srk object" << endl;
		if(b_log)	cerr << ' ' << result << " create srk object" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_srk);
		if(b_debug)	cerr << ' ' << result << " create srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " create srk policy" << endl;

	result = Tspi_Policy_SetSecret(policy_srk, TSS_SECRET_MODE_PLAIN, wks_size, wks_blob);
		if(b_debug)	cerr << ' ' << result << " set srk auth" << endl;
		if(b_log)	cerr << ' ' << result << " set srk auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_srk, srk);
		if(b_debug)	cerr << ' ' << result << " assign srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " assign srk policy" << endl;


//EK SECTION
	TSS_HKEY		ek;
	TSS_HPOLICY		policy_ek;
	TSS_VALIDATION	validation;

		if(b_debug)	cerr << "EK Section" << endl;
		if(b_log)	clog << "EK Section" << endl;

	memset(&validation, 0, sizeof(TSS_VALIDATION));
	validation.versionInfo.bMajor = 0x01;
	validation.versionInfo.bMinor = 0x02;
	validation.versionInfo.bRevMajor = 0x01;
	validation.versionInfo.bRevMinor = 0x25;
	validation.ulExternalDataLength = sizeof(TSS_NONCE);
	validation.rgbExternalData = nonce.blob;
	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_SIZE_DEFAULT, &ek);
		if(b_debug)	cerr << ' ' << result << " create ek object" << endl;
		if(b_log)	cerr << ' ' << result << " create ek object" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_ek);
		if(b_debug)	cerr << ' ' << result << " create ek policy" << endl;
		if(b_log)	cerr << ' ' << result << " create ek policy" << endl;

	result = Tspi_Policy_SetSecret(policy_ek, TSS_SECRET_MODE_PLAIN, ownerauth.size, ownerauth.blob);
		if(b_debug)	cerr << ' ' << result << " set auth" << endl;
		if(b_log)	cerr << ' ' << result << " set auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_ek, ek);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;

	result = Tspi_TPM_GetPubEndorsementKey(tpm, false, &validation, &ek);
		if(b_debug)	cerr << ' ' << result << " get the ek (false flag)" << endl;
		if(b_log)	cerr << ' ' << result << " get the ek (false flag)" << endl;


//TAKE OWNERSHIP
		if(b_debug)	cerr << "Take Ownership Section" << endl;
		if(b_log)	clog << "Take Ownership Section" << endl;

	result = Tspi_TPM_TakeOwnership(tpm, srk, ek);
		if(b_debug)	cerr << ' ' << result << " TAKE OWNERSHIP" << endl;
		if(b_log)	cerr << ' ' << result << " TAKE OWNERSHIP" << endl;
	return_code = result;

	if(result == 0)
	{
	//LOAD KEYS
		result = Tspi_Context_LoadKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_srk, &srk);
			if(b_debug)	cerr << ' ' << result << " save the srk" << endl;
			if(b_log)	cerr << ' ' << result << " save the srk" << endl;
		return_code = result;
	}


//CLEANUP
	result = Tspi_Context_FreeMemory(context, validation.rgbData);
	result = Tspi_Context_FreeMemory(context, validation.rgbValidationData);
	result = Tspi_Context_FreeMemory(context, validation.rgbExternalData);

	result = Tspi_Context_CloseObject(context, policy_tpm);
	result = Tspi_Context_CloseObject(context, policy_ek);
	result = Tspi_Context_CloseObject(context, srk);
	result = Tspi_Context_CloseObject(context, ek);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Clear Ownership
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::clear_ownership()
{
	if(b_help)
	{
		cout << "Clear Ownership (" << i_mode << ") --- Clears ownership data and deactivates TPM" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -owner_auth (hex blob, owner authorization)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	string				s_ownerauth;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-owner_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_ownerauth = s_argv[i];
			i_success++;
		}
	}
	if(i_success != 1)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	ownerauth(s_ownerauth);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;
	UINT32				wks_size = sizeof(wks_blob);


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;
	TSS_HPOLICY		policy_tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_tpm);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Policy_SetSecret(policy_tpm, TSS_SECRET_MODE_PLAIN, ownerauth.size, ownerauth.blob);
		if(b_debug)	cerr << ' ' << result << " owner auth" << endl;
		if(b_log)	cerr << ' ' << result << " owner auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_tpm, tpm);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;


//CLEAR OWNERSHIP
		if(b_debug)	cerr << "Clear Ownership Section" << endl;
		if(b_log)	clog << "Clear Ownership Section" << endl;

	result = Tspi_TPM_ClearOwner(tpm, FALSE);
		if(b_debug)	cerr << ' ' << result << " CLEAR OWNERSHIP" << endl;
		if(b_log)	cerr << ' ' << result << " CLEAR OWNERSHIP" << endl;
	return_code = result;


//CLEANUP
	result = Tspi_Context_CloseObject(context, policy_tpm);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Collate Identity
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::collate_identity()
{
	if(b_help)
	{
		cout << "Collate Identity (" << i_mode << ") --- Creates an Attestation Identity Key (AIK)" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -owner_auth (hex blob, owner authorization)" << endl;
		cout << " -key_auth (hex blob, identity key authorization data)" << endl;
		cout << " -key_label (hex blob, hex representation of aik label)" << endl;
		cout << " -pcak (hex blob, privacy CA key)" << endl;
		cout << " -key_index (integer, index number for key)" << endl;
		cout << " OPTIONAL PARAMETERS" << endl;
		cout << " -ec_blob (hex blob, endorsement credential)" << endl;
		cout << " -ec_nvram (flag, forces endorsement credential to load from NVRAM)" << endl;
		cout << " -trousers (flag, manually determines credential size from DER x509 size header)" << endl;
		cout << " OUTPUTS" << endl;
		cout << " identity request (hex blob)" << endl;
		cout << " modulus (hex blob, key modulus)" << endl;
		cout << " key blob (hex blob, key blob)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	string				s_ownerauth;
	string				s_pcak;
	string				s_aiklabel;
	string				s_aikauth;
	string				s_ec;
	int					i_keyindex = 0;
	bool				ec_nvram = false;
	bool				ec_blob = false;
	bool				b_trousers = false;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-owner_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_ownerauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-pcak") == 0)
		{
			if(++i >= i_argc) return;
			s_pcak = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_label") == 0)
		{
			if(++i >= i_argc) return;
			s_aiklabel = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_aikauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_index") == 0)
		{
			if(++i >= i_argc) return;
			i_keyindex = atoi(s_argv[i].c_str());
			i_success++;
		}

		//explicitly load endorsement credential via hex blob
		if(s_argv[i].compare("-ec_blob") == 0)
		{
			if(++i >= i_argc) return;
			s_ec = s_argv[i];
			ec_blob = true;
		}

		//explicitly load endorsement credential through NVRAM
		if(s_argv[i].compare("-ec_nvram") == 0)
		{
			ec_nvram = true;
		}

		if(s_argv[i].compare("-trousers") == 0)
		{
			b_trousers = true;
		}
	}
	if(i_success != 5)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	ownerauth(s_ownerauth);
	NIARL_Util_ByteBlob	pcakblob(s_pcak);
	NIARL_Util_ByteBlob	aiklabel(s_aiklabel);
	NIARL_Util_ByteBlob	aikauth(s_aikauth);
	NIARL_Util_ByteBlob	ec(s_ec);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;
	UINT32				wks_size = sizeof(wks_blob);

	TSS_UUID			uuid_aik = TSS_UUID_USK2;
	uuid_aik.rgbNode[5] = (BYTE)i_keyindex;
	uuid_aik.rgbNode[0] = 0x04;


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;
	TSS_HPOLICY		policy_tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_tpm);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Policy_SetSecret(policy_tpm, TSS_SECRET_MODE_PLAIN, ownerauth.size, ownerauth.blob);
		if(b_debug)	cerr << ' ' << result << " owner auth" << endl;
		if(b_log)	cerr << ' ' << result << " owner auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_tpm, tpm);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;


//SRK OPERATIONS (SET)
	TSS_HKEY		srk;
	TSS_HPOLICY		policy_srk;
	TSS_UUID		uuid_srk = TSS_UUID_SRK;

		if(b_debug)	cerr << "SRK Section" << endl;
		if(b_log)	clog << "SRK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_TSP_SRK, &srk);
		if(b_debug)	cerr << ' ' << result << " create srk object" << endl;
		if(b_log)	cerr << ' ' << result << " create srk object" << endl;

	result = Tspi_Context_LoadKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_srk, &srk);
		if(b_debug)	cerr << ' ' << result << " load srk by uuid" << endl;
		if(b_log)	cerr << ' ' << result << " load srk by uuid" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_srk);
		if(b_debug)	cerr << ' ' << result << " create srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " create srk policy" << endl;

	result = Tspi_Policy_SetSecret(policy_srk, TSS_SECRET_MODE_PLAIN, wks_size, wks_blob);
		if(b_debug)	cerr << ' ' << result << " set srk auth" << endl;
		if(b_log)	cerr << ' ' << result << " set srk auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_srk, srk);
		if(b_debug)	cerr << ' ' << result << " assign srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " assign srk policy" << endl;


//PRIVACY CA SECTION
	TSS_HKEY		pcak;
	TSS_HPOLICY		policy_pcak;

		if(b_debug)	cerr << "PCAK Section" << endl;
		if(b_log)	clog << "PCAK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_SIZE_DEFAULT, &pcak);
		if(b_debug)	cerr << ' ' << result << " create pcak object" << endl;
		if(b_log)	cerr << ' ' << result << " create pcak object" << endl;

	result = Tspi_SetAttribData(pcak, TSS_TSPATTRIB_KEY_BLOB, TSS_TSPATTRIB_KEYBLOB_PUBLIC_KEY, pcakblob.size, pcakblob.blob);
		if(b_debug)	cerr << ' ' << result << " set pcak blob" << endl;
		if(b_log)	cerr << ' ' << result << " set pcak blob" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_pcak);
		if(b_debug)	cerr << ' ' << result << " create pcak policy" << endl;
		if(b_log)	cerr << ' ' << result << " create pcak policy" << endl;

	result = Tspi_Policy_SetSecret(policy_pcak, TSS_SECRET_MODE_PLAIN, ownerauth.size, ownerauth.blob);
		if(b_debug)	cerr << ' ' << result << " set auth" << endl;
		if(b_log)	cerr << ' ' << result << " set auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_pcak, pcak);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;


//AIK OPERATIONS (NOT SET YET)
	TSS_HKEY		aik;
	TSS_HPOLICY		policy_aik;
	UINT32			init_flags	= TSS_KEY_TYPE_IDENTITY | TSS_KEY_SIZE_2048  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;

		if(b_debug)	cerr << "AIK Section" << endl;
		if(b_log)	clog << "AIK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, init_flags, &aik);
		if(b_debug)	cerr << ' ' << result << " create aik object" << endl;
		if(b_log)	cerr << ' ' << result << " create aik object" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_aik);
		if(b_debug)	cerr << ' ' << result << " create aik policy" << endl;
		if(b_log)	cerr << ' ' << result << " create aik policy" << endl;

	result = Tspi_Policy_SetSecret(policy_aik, TSS_SECRET_MODE_PLAIN, aikauth.size, aikauth.blob);
		if(b_debug)	cerr << ' ' << result << " set aik auth" << endl;
		if(b_log)	cerr << ' ' << result << " set aik auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_aik, aik);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;


//ENDORSEMENT CREDENTIAL COMMANDS
	if(ec_blob)
	{
		//explicitly load the endorsement credential from command line
		result = Tspi_SetAttribData(tpm, TSS_TSPATTRIB_TPM_CREDENTIAL, TSS_TPMATTRIB_EKCERT, ec.size, ec.blob);
			if(b_debug)	cerr << ' ' << result << " load endorsement credential by command line" << endl;
			if(b_log)	cerr << ' ' << result << " load endorsement credential by command line" << endl;
	}
	else if(ec_nvram)
	{
	//NVSTORE SECTION
		TSS_HNVSTORE	nvstore;
		UINT32			cred_size;
		BYTE*			cred_blob;

			if(b_debug)	cerr << "NVStore Section" << endl;
			if(b_log)	clog << "NVStore Section" << endl;

		result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_NV, NULL, &nvstore);
			if(b_debug)	cerr << ' ' << result << " create nvstore object" << endl;
			if(b_log)	cerr << ' ' << result << " create nvstore object" << endl;

		result = Tspi_Policy_AssignToObject(policy_tpm, nvstore);
			if(b_debug)	cerr << ' ' << result << " assign owner auth" << endl;
			if(b_log)	cerr << ' ' << result << " assign owner auth" << endl;

		result = Tspi_SetAttribUint32(nvstore, TSS_TSPATTRIB_NV_INDEX, NULL, TPM_NV_INDEX_EKCert);
			if(b_debug)	cerr << " EK cert selected" << endl;
			if(b_log)	clog << " EK cert selected" << endl;

		if(b_trousers)
		{//Trousers mode. Size cannot be automatically determined
			if(b_debug)	cerr << " Trousers mode activated" << endl;
			if(b_log)	clog << " Trousers mode activated" << endl;

			UINT32			counter = 0;

			cred_size = 10;		//allow enough space to get DER x509 size header
			result = Tspi_NV_ReadValue(nvstore, 0, &cred_size, &cred_blob);

			if((int)cred_blob[1] >= 128)	//size is too big for [1]
			{
				counter = (int)cred_blob[1] - 128;
				cred_size = 0;				//reset cred size

				for(UINT32 i = 0; i < counter; i++)
				{
					cred_size *= 256;					//base multiplier
					cred_size += (int)cred_blob[2 + i];	//accumulator
				}
			}
			else
			{
				cred_size = (int)cred_blob[1];
			}

			cred_size += 4;

			if(b_debug)	cerr << " Credential size is " << cred_size << endl;
			if(b_log)	clog << " Credential size is " << cred_size << endl;
		}
		else
		{//NTru mode
			result = Tspi_GetAttribUint32(nvstore, TSS_TSPATTRIB_NV_DATASIZE, NULL, &cred_size);
				if(b_debug)	cerr << ' ' << result << " get nvstore size of " << cred_size << endl;
				if(b_log)	cerr << ' ' << result << " get nvstore size of " << cred_size << endl;
		}

		result = Tspi_NV_ReadValue(nvstore, 0, &cred_size, &cred_blob);
			if(b_debug)	cerr << ' ' << result << " nv read" << endl;
			if(b_log)	cerr << ' ' << result << " nv read" << endl;

		//explicitly load the endorsement credential from NVRAM
		result = Tspi_SetAttribData(tpm, TSS_TSPATTRIB_TPM_CREDENTIAL, TSS_TPMATTRIB_EKCERT, cred_size, cred_blob);
			if(b_debug)	cerr << ' ' << result << " load endorsement credential by NVRAM" << endl;
			if(b_log)	cerr << ' ' << result << " load endorsement credential by NVRAM" << endl;

		result = Tspi_Context_FreeMemory(context, cred_blob);
	}


//COLLATE IDENTITY REQUEST
	UINT32			idr_size;
	BYTE*			idr_blob;

		if(b_debug)	cerr << "Collate Identity Request Section" << endl;
		if(b_log)	clog << "Collate Identity Request Section" << endl;

	result = Tspi_TPM_CollateIdentityRequest(tpm, srk, pcak, aiklabel.size, aiklabel.blob, aik, TSS_ALG_AES, &idr_size, &idr_blob);
		if(b_debug)	cerr << ' ' << result << " COLLATE IDENTITY" << endl;
		if(b_log)	cerr << ' ' << result << " COLLATE IDENTITY" << endl;

	if(result == 0)
	{
		for(UINT32 i = 0; i < idr_size; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)idr_blob[i];
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		result = Tspi_Context_FreeMemory(context, idr_blob);
	}


//OUTPUT AIK MODULUS AND BLOB
	UINT32			mod_size;
	UINT32			blob_size;
	BYTE*			mod_blob;
	BYTE*			blob_blob;

		if(b_debug)	cerr << "AIK Output Section" << endl;
		if(b_log)	clog << "AIK Output Section" << endl;

	result = Tspi_GetAttribData(aik, TSS_TSPATTRIB_RSAKEY_INFO, TSS_TSPATTRIB_KEYINFO_RSA_MODULUS, &mod_size, &mod_blob);
		if(b_debug)	cerr << ' ' << result << " get modulus" << endl;
		if(b_log)	cerr << ' ' << result << " get modulus" << endl;

	if(result == 0)
	{
		if(!b_debug && !b_log) if(!b_debug && !b_log) cout << ' ';

		for(UINT32 i = 0; i < mod_size; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)mod_blob[i];
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		result = Tspi_Context_FreeMemory(context, mod_blob);
	}

	result = Tspi_GetAttribData(aik, TSS_TSPATTRIB_KEY_BLOB, TSS_TSPATTRIB_KEYBLOB_BLOB, &blob_size, &blob_blob);
		if(b_debug)	cerr << ' ' << result << " get key blob" << endl;
		if(b_log)	cerr << ' ' << result << " get key blob" << endl;

	if(result == 0)
	{
		if(!b_debug && !b_log) if(!b_debug && !b_log) cout << ' ';

		for(UINT32 i = 0; i < blob_size; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)blob_blob[i];
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		result = Tspi_Context_FreeMemory(context, blob_blob);
	}


//SAVE THE AIK
	TSS_HKEY		key_blank;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_SIZE_DEFAULT, &key_blank);
		if(b_debug)	cerr << ' ' << result << " create blank key" << endl;
		if(b_log)	cerr << ' ' << result << " create blank key" << endl;

	result = Tspi_Key_LoadKey(aik, srk);
		if(b_debug)	cerr << ' ' << result << " load the aik" << endl;
		if(b_log)	cerr << ' ' << result << " load the aik" << endl;

	result = Tspi_Context_RegisterKey(context, aik, TSS_PS_TYPE_SYSTEM, uuid_aik, TSS_PS_TYPE_SYSTEM, uuid_srk);
		if(b_debug)	cerr << ' ' << result << " register aik" << endl;
		if(b_log)	cerr << ' ' << result << " register aik" << endl;
		return_code = result;

	if(result != 0)
	{
		result = Tspi_Context_UnregisterKey(context, TSS_PS_TYPE_SYSTEM, uuid_aik, &key_blank);
			if(b_debug)	cerr << ' ' << result << " unregister blank key" << endl;
			if(b_log)	cerr << ' ' << result << " unregister blank key" << endl;

		result = Tspi_Context_RegisterKey(context, aik, TSS_PS_TYPE_SYSTEM, uuid_aik, TSS_PS_TYPE_SYSTEM, uuid_srk);
			if(b_debug)	cerr << ' ' << result << " register aik" << endl;
			if(b_log)	cerr << ' ' << result << " register aik" << endl;
			return_code = result;
	}


//CLEANUP SECTION
	result = Tspi_Context_CloseObject(context, policy_tpm);
	result = Tspi_Context_CloseObject(context, policy_aik);
	result = Tspi_Context_CloseObject(context, policy_pcak);
	result = Tspi_Context_CloseObject(context, aik);
	result = Tspi_Context_CloseObject(context, pcak);
	result = Tspi_Context_CloseObject(context, srk);
	result = Tspi_Context_CloseObject(context, key_blank);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Activate Identity
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::activate_identity()
{
	if(b_help)
	{
		cout << "Activate Identity (" << i_mode << ") --- Creates an Attestation Identity Credential (AIC)" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -owner_auth (hex blob, owner authorization)" << endl;
		cout << " -key_auth (hex blob, identity key authorization data)" << endl;
		cout << " -asym (hex blob, CA asymmetric response)" << endl;
		cout << " -sym (hex blob, CA symmetric response)" << endl;
		cout << " -key_index (integer, index number for key)" << endl;
		cout << " OUTPUTS" << endl;
		cout << " attestation identity credential (AIC, hex blob)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	string				s_ownerauth;
	string				s_asym;
	string				s_sym;
	string				s_aikauth;
	int					i_keyindex = 0;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-owner_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_ownerauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-asym") == 0)
		{
			if(++i >= i_argc) return;
			s_asym = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-sym") == 0)
		{
			if(++i >= i_argc) return;
			s_sym = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_aikauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_index") == 0)
		{
			if(++i >= i_argc) return;
			i_keyindex = atoi(s_argv[i].c_str());
			i_success++;
		}
	}
	if(i_success != 5)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	ownerauth(s_ownerauth);
	NIARL_Util_ByteBlob	aikauth(s_aikauth);
	NIARL_Util_ByteBlob	ca_sym(s_sym);
	NIARL_Util_ByteBlob	ca_asym(s_asym);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;
	UINT32				wks_size = sizeof(wks_blob);

	TSS_UUID			uuid_aik = TSS_UUID_USK2;
	uuid_aik.rgbNode[5] = (BYTE)i_keyindex;
	uuid_aik.rgbNode[0] = 0x04;


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;
	TSS_HPOLICY		policy_tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_tpm);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Policy_SetSecret(policy_tpm, TSS_SECRET_MODE_PLAIN, ownerauth.size, ownerauth.blob);
		if(b_debug)	cerr << ' ' << result << " owner auth" << endl;
		if(b_log)	cerr << ' ' << result << " owner auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_tpm, tpm);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;


//SRK OPERATIONS (SET)
	TSS_HKEY		srk;
	TSS_HPOLICY		policy_srk;
	TSS_UUID		uuid_srk = TSS_UUID_SRK;

		if(b_debug)	cerr << "SRK Section" << endl;
		if(b_log)	clog << "SRK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_TSP_SRK, &srk);
		if(b_debug)	cerr << ' ' << result << " create srk object" << endl;
		if(b_log)	cerr << ' ' << result << " create srk object" << endl;

	result = Tspi_Context_LoadKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_srk, &srk);
		if(b_debug)	cerr << ' ' << result << " load srk by uuid" << endl;
		if(b_log)	cerr << ' ' << result << " load srk by uuid" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_srk);
		if(b_debug)	cerr << ' ' << result << " create srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " create srk policy" << endl;

	result = Tspi_Policy_SetSecret(policy_srk, TSS_SECRET_MODE_PLAIN, wks_size, wks_blob);
		if(b_debug)	cerr << ' ' << result << " set srk auth" << endl;
		if(b_log)	cerr << ' ' << result << " set srk auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_srk, srk);
		if(b_debug)	cerr << ' ' << result << " assign srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " assign srk policy" << endl;


//AIK OPERATIONS (SET)
	TSS_HKEY		aik;
	TSS_HPOLICY		policy_aik;
	UINT32			init_flags	= TSS_KEY_TYPE_IDENTITY | TSS_KEY_SIZE_2048  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;

		if(b_debug)	cerr << "AIK Section" << endl;
		if(b_log)	clog << "AIK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, init_flags, &aik);
		if(b_debug)	cerr << ' ' << result << " create aik object" << endl;
		if(b_log)	cerr << ' ' << result << " create aik object" << endl;

	result = Tspi_Context_GetKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_aik, &aik);
		if(b_debug)	cerr << ' ' << result << " get uuid" << endl;
		if(b_log)	cerr << ' ' << result << " get uuid" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_aik);
		if(b_debug)	cerr << ' ' << result << " create aik policy" << endl;
		if(b_log)	cerr << ' ' << result << " create aik policy" << endl;

	result = Tspi_Policy_SetSecret(policy_aik, TSS_SECRET_MODE_PLAIN, aikauth.size, aikauth.blob);
		if(b_debug)	cerr << ' ' << result << " set aik auth" << endl;
		if(b_log)	cerr << ' ' << result << " set aik auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_aik, aik);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;

	result = Tspi_Key_LoadKey(aik, srk);
		if(b_debug)	cerr << ' ' << result << " load aik" << endl;
		if(b_log)	cerr << ' ' << result << " load aik" << endl;


//ACTIVATE IDENTITY
	UINT32			aic_size;
	BYTE*			aic_blob;

		if(b_debug)	cerr << "Activate Identity Section" << endl;
		if(b_log)	clog << "Activate Identity Section" << endl;

	result = Tspi_TPM_ActivateIdentity(tpm, aik, ca_asym.size, ca_asym.blob, ca_sym.size, ca_sym.blob, &aic_size, &aic_blob);
		if(b_debug)	cerr << ' ' << result << " ACTIVATE IDENTITY" << endl;
		if(b_log)	cerr << ' ' << result << " ACTIVATE IDENTITY" << endl;
		return_code = result;

	if(result == 0)
	{
		for(UINT32 i = 0; i < aic_size; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)aic_blob[i];
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		result = Tspi_Context_FreeMemory(context, aic_blob);
	}


//CLEANUP SECTION
	result = Tspi_Context_CloseObject(context, policy_tpm);
	result = Tspi_Context_CloseObject(context, policy_aik);
	result = Tspi_Context_CloseObject(context, aik);
	result = Tspi_Context_CloseObject(context, srk);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Quote
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::quote()
{
	if(b_help)
	{
		cout << "Quote (" << i_mode << ") --- Provides a system integrity quote with signature" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -key_auth (hex blob, identity key authorization data)" << endl;
		cout << " -nonce (hex blob, anti-replay nonce)" << endl;
		cout << " -mask (hex string, controls PCR index selection)" << endl;
		cout << " -key_index (integer, index number for key)" << endl;
		cout << " OUTPUTS" << endl;
		cout << " quote (hex blob, quote digest)" << endl;
		cout << " signature (hex blob, quote signature)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	string				s_pcrs;
	string				s_aikauth;
	string				s_nonce;
	int					i_keyindex = 0;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-nonce") == 0)
		{
			if(++i >= i_argc) return;
			s_nonce = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_aikauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-mask") == 0)
		{
			if(++i >= i_argc) return;
			s_pcrs = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_index") == 0)
		{
			if(++i >= i_argc) return;
			i_keyindex = atoi(s_argv[i].c_str());
			i_success++;
		}
	}
	if(i_success != 4)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	aikauth(s_aikauth);
	NIARL_Util_ByteBlob	nonce(s_nonce);
	NIARL_Util_Mask		pcrmask(s_pcrs);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;
	UINT32				wks_size = sizeof(wks_blob);

	TSS_UUID			uuid_aik = TSS_UUID_USK2;
	uuid_aik.rgbNode[5] = (BYTE)i_keyindex;
	uuid_aik.rgbNode[0] = 0x04;


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;


//SRK OPERATIONS (SET)
	TSS_HKEY		srk;
	TSS_HPOLICY		policy_srk;
	TSS_UUID		uuid_srk = TSS_UUID_SRK;

		if(b_debug)	cerr << "SRK Section" << endl;
		if(b_log)	clog << "SRK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_TSP_SRK, &srk);
		if(b_debug)	cerr << ' ' << result << " create srk object" << endl;
		if(b_log)	cerr << ' ' << result << " create srk object" << endl;

	result = Tspi_Context_LoadKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_srk, &srk);
		if(b_debug)	cerr << ' ' << result << " load srk by uuid" << endl;
		if(b_log)	cerr << ' ' << result << " load srk by uuid" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_srk);
		if(b_debug)	cerr << ' ' << result << " create srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " create srk policy" << endl;

	result = Tspi_Policy_SetSecret(policy_srk, TSS_SECRET_MODE_PLAIN, wks_size, wks_blob);
		if(b_debug)	cerr << ' ' << result << " set srk auth" << endl;
		if(b_log)	cerr << ' ' << result << " set srk auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_srk, srk);
		if(b_debug)	cerr << ' ' << result << " assign srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " assign srk policy" << endl;


//AIK OPERATIONS (SET)
	TSS_HKEY		aik;
	TSS_HPOLICY		policy_aik;
	UINT32			init_flags	= TSS_KEY_TYPE_IDENTITY | TSS_KEY_SIZE_2048  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;

		if(b_debug)	cerr << "AIK Section" << endl;
		if(b_log)	clog << "AIK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, init_flags, &aik);
		if(b_debug)	cerr << ' ' << result << " create aik object" << endl;
		if(b_log)	cerr << ' ' << result << " create aik object" << endl;

	result = Tspi_Context_GetKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_aik, &aik);
		if(b_debug)	cerr << ' ' << result << " get uuid" << endl;
		if(b_log)	cerr << ' ' << result << " get uuid" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_aik);
		if(b_debug)	cerr << ' ' << result << " create aik policy" << endl;
		if(b_log)	cerr << ' ' << result << " create aik policy" << endl;

	result = Tspi_Policy_SetSecret(policy_aik, TSS_SECRET_MODE_PLAIN, aikauth.size, aikauth.blob);
		if(b_debug)	cerr << ' ' << result << " set aik auth" << endl;
		if(b_log)	cerr << ' ' << result << " set aik auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_aik, aik);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;

	result = Tspi_Key_LoadKey(aik, srk);
		if(b_debug)	cerr << ' ' << result << " load aik" << endl;
		if(b_log)	cerr << ' ' << result << " load aik" << endl;


//PCR OPERATIONS
	TSS_HPCRS		pcr;
	UINT32			pcr_size;
	BYTE*			pcr_blob;

		if(b_debug)	cerr << "PCR Section" << endl;
		if(b_log)	clog << "PCR Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_PCRS, 0, &pcr);
		if(b_debug)	cerr << ' ' << result << " create pcr object" << endl;
		if(b_log)	cerr << ' ' << result << " create pcr object" << endl;

	for(UINT32 i = 0; i < pcrmask.size; i++)
	{
		result = Tspi_TPM_PcrRead(tpm, pcrmask.index[i], &pcr_size, &pcr_blob);
			if(b_debug)	cerr << ' ' << result << " read pcr index " << pcrmask.index[i] << endl;
			if(b_log)	cerr << ' ' << result << " read pcr index " << pcrmask.index[i] << endl;

		result = Tspi_PcrComposite_SelectPcrIndex(pcr, pcrmask.index[i]);
			if(b_debug)	cerr << ' ' << result << " select composite index" << endl;
			if(b_log)	cerr << ' ' << result << " select composite index" << endl;

		result = Tspi_PcrComposite_SetPcrValue(pcr, pcrmask.index[i], pcr_size, pcr_blob);
			if(b_debug)	cerr << ' ' << result << " set pcr value" << endl;
			if(b_log)	cerr << ' ' << result << " set pcr value" << endl;

		for(UINT32 j = 0; j < pcr_size; j++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)pcr_blob[j];
		if(!b_debug && !b_log) cout << ' ';
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		result = Tspi_Context_FreeMemory(context, pcr_blob);
			if(b_debug)	cerr << ' ' << result << " clear dynamic array" << endl;
			if(b_log)	cerr << ' ' << result << " clear dynamic array" << endl;
	}


//QUOTE OPERATIONS
	TSS_VALIDATION	validation;

		if(b_debug)	cerr << "Quote Section" << endl;
		if(b_log)	clog << "Quote Section" << endl;

	memset(&validation, 0, sizeof(TSS_VALIDATION));
	validation.versionInfo.bMajor = 0x01;
	validation.versionInfo.bMinor = 0x02;
	validation.versionInfo.bRevMajor = 0x01;
	validation.versionInfo.bRevMinor = 0x25;
	validation.ulExternalDataLength = sizeof(TSS_NONCE);
	validation.rgbExternalData = nonce.blob;

	result = Tspi_TPM_Quote(tpm, aik, pcr, &validation);
		if(b_debug)	cerr << ' ' << result << " QUOTE" << endl;
		if(b_log)	cerr << ' ' << result << " QUOTE" << endl;
		return_code = result;

	if(result == 0)
	{
		for(UINT32 i = 0; i < validation.ulExternalDataLength; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)validation.rgbExternalData[i];

		if(!b_debug && !b_log) cout << ' ';
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		for(UINT32 i = 0; i < validation.ulDataLength; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)validation.rgbData[i];

		if(!b_debug && !b_log) cout << ' ';
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		for(UINT32 i = 0; i < validation.ulValidationDataLength; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)validation.rgbValidationData[i];
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;
	}


//CLEANUP SECTION
	result = Tspi_Context_FreeMemory(context, validation.rgbData);
	result = Tspi_Context_FreeMemory(context, validation.rgbValidationData);
	result = Tspi_Context_FreeMemory(context, validation.rgbExternalData);

	result = Tspi_Context_CloseObject(context, policy_aik);
	result = Tspi_Context_CloseObject(context, aik);
	result = Tspi_Context_CloseObject(context, srk);
	result = Tspi_Context_CloseObject(context, pcr);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Create Revocable EK
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::create_revokable_ek()
{
	if(b_help)
	{
		cout << "Create Revocable EK (" << i_mode << ") --- Creates a revocable EK" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -owner_auth (hex blob, owner authorization)" << endl;
		cout << " -reset (hex blob, reset authorization blob)" << endl;
		cout << " -nonce (hex blob, anti-replay nonce)" << endl;
		cout << " -key_index (integer, index number for key)" << endl;
		cout << " OUTPUTS" << endl;
		cout << " Validation data?" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


/********************
	MODE DISABLED
********************/
throw ERROR_MODE_DISABLED;


//DYNAMIC CONTENT
	short				i_success = 0;
	string				s_ownerauth;
	string				s_nonce;
	string				s_reset;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-owner_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_ownerauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-nonce") == 0)
		{
			if(++i >= i_argc) return;
			s_nonce = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-reset") == 0)
		{
			if(++i >= i_argc) return;
			s_reset = s_argv[i];
			i_success++;
		}
	}
	if(i_success != 3)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	ownerauth(s_ownerauth);
	NIARL_Util_ByteBlob	nonce(s_nonce);
	NIARL_Util_ByteBlob	reset(s_reset);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;
	UINT32				wks_size = sizeof(wks_blob);


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;
	TSS_HPOLICY		policy_tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_tpm);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Policy_SetSecret(policy_tpm, TSS_SECRET_MODE_PLAIN, ownerauth.size, ownerauth.blob);
		if(b_debug)	cerr << ' ' << result << " owner auth" << endl;
		if(b_log)	cerr << ' ' << result << " owner auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_tpm, tpm);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;


//REK SECTION
	TSS_HKEY		rek;
	TSS_HPOLICY		policy_rek;

		if(b_debug)	cerr << "REK Section" << endl;
		if(b_log)	clog << "REK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_SIZE_DEFAULT, &rek);
		if(b_debug)	cerr << ' ' << result << " create rek object" << endl;
		if(b_log)	cerr << ' ' << result << " create rek object" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_rek);
		if(b_debug)	cerr << ' ' << result << " create rek policy" << endl;
		if(b_log)	cerr << ' ' << result << " create rek policy" << endl;

	result = Tspi_Policy_SetSecret(policy_rek, TSS_SECRET_MODE_PLAIN, ownerauth.size, ownerauth.blob);
		if(b_debug)	cerr << ' ' << result << " set rek auth" << endl;
		if(b_log)	cerr << ' ' << result << " set rek auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_rek, rek);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;


//CREATE REVOCABLE EK
	TSS_VALIDATION	validation;

		if(b_debug)	cerr << "Create REK Section" << endl;
		if(b_log)	clog << "Create REK Section" << endl;

	memset(&validation, 0, sizeof(TSS_VALIDATION));
	validation.versionInfo.bMajor = 0x01;
	validation.versionInfo.bMinor = 0x02;
	validation.versionInfo.bRevMajor = 0x01;
	validation.versionInfo.bRevMinor = 0x25;
	validation.ulExternalDataLength = sizeof(TSS_NONCE);
	validation.rgbExternalData = nonce.blob;

	result = Tspi_TPM_CreateRevocableEndorsementKey(tpm, rek, &validation, &reset.size, &reset.blob);
		if(b_debug)	cerr << ' ' << result << " CREATE REK" << endl;
		if(b_log)	cerr << ' ' << result << " CREATE REK" << endl;
		return_code = result;

	if(result == 0)
	{
		for(UINT32 i = 0; i < validation.ulExternalDataLength; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)validation.rgbExternalData[i];

		if(!b_debug && !b_log) cout << ' ';
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		for(UINT32 i = 0; i < validation.ulDataLength; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)validation.rgbData[i];

		if(!b_debug && !b_log) cout << ' ';
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		for(UINT32 i = 0; i < validation.ulValidationDataLength; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)validation.rgbValidationData[i];
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;
	}


//CLEANUP SECTION
	result = Tspi_Context_FreeMemory(context, validation.rgbData);
	result = Tspi_Context_FreeMemory(context, validation.rgbValidationData);
	result = Tspi_Context_FreeMemory(context, validation.rgbExternalData);

	result = Tspi_Context_CloseObject(context, policy_tpm);
	result = Tspi_Context_CloseObject(context, policy_rek);
	result = Tspi_Context_CloseObject(context, rek);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Revoke EK
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::revoke_ek()
{
	if(b_help)
	{
		cout << "Revoke EK (" << i_mode << ") --- Revokes a revocable EK" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -owner_auth (hex blob, owner authorization)" << endl;
		cout << " -reset (hex blob, reset authorization blob)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


/********************
	MODE DISABLED
********************/
throw ERROR_MODE_DISABLED;


//DYNAMIC CONTENT
	short				i_success = 0;
	string				s_ownerauth;
	string				s_reset;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-owner_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_ownerauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-reset") == 0)
		{

			if(++i >= i_argc) return;
			s_reset = s_argv[i];
			i_success++;
		}
	}
	if(i_success != 2)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	ownerauth(s_ownerauth);
	NIARL_Util_ByteBlob	reset(s_reset);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;
	UINT32				wks_size = sizeof(wks_blob);


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;
	TSS_HPOLICY		policy_tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_tpm);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Policy_SetSecret(policy_tpm, TSS_SECRET_MODE_PLAIN, ownerauth.size, ownerauth.blob);
		if(b_debug)	cerr << ' ' << result << " owner auth" << endl;
		if(b_log)	cerr << ' ' << result << " owner auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_tpm, tpm);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;


//CREATE REVOCABLE EK

		if(b_debug)	cerr << "Revoke REK Section" << endl;
		if(b_log)	clog << "Revoke REK Section" << endl;

	result = Tspi_TPM_RevokeEndorsementKey(tpm, reset.size, reset.blob);
		if(b_debug)	cerr << ' ' << result << " REVOKE REK" << endl;
		if(b_log)	cerr << ' ' << result << " REVOKE REK" << endl;
		return_code = result;


//CLEANUP SECTION
	result = Tspi_Context_CloseObject(context, policy_tpm);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Create Key
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::create_key()
{
	if(b_help)
	{
		cout << "Create Key (" << i_mode << ") --- Creates and stores (no overwrite) a binding or signing key" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -key_type (string, sign or bind)" << endl;
		cout << " -key_auth (hex blob, key authorization data)" << endl;
		cout << " -key_index (integer, index number for key)" << endl;
		cout << " OPTIONAL PARAMETERS" << endl;
		cout << " -1024 (flag, uses 1024 keys instead of 2048)" << endl;
		cout << " -pkcs (flag, switches encoding from OAEP to PKCS for more space)" << endl;
		cout << " OUTPUTS" << endl;
		cout << " modulus (hex blob, key modulus)" << endl;
		cout << " key blob (hex blob, key blob)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	string				keytype;
	string				s_keyauth;
	int					i_keyindex = 0;
	bool				b_1024 = false;
	bool				b_pkcs = false;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-1024") == 0)
		{
			b_1024 = true;
		}

		if(s_argv[i].compare("-pkcs") == 0)
		{
			b_pkcs = true;
		}

		if(s_argv[i].compare("-key_type") == 0)
		{
			if(++i >= i_argc) return;
			keytype = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_keyauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_index") == 0)
		{
			if(++i >= i_argc) return;
			i_keyindex = atoi(s_argv[i].c_str());
			i_success++;
		}
	}
	if(i_success != 3)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	keyauth(s_keyauth);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;
	UINT32				wks_size = sizeof(wks_blob);

	TSS_UUID			uuid_key = TSS_UUID_USK2;
	uuid_key.rgbNode[5] = (BYTE)i_keyindex;


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;


//SRK OPERATIONS (SET)
	TSS_HKEY		srk;
	TSS_HPOLICY		policy_srk;
	TSS_UUID		uuid_srk = TSS_UUID_SRK;

		if(b_debug)	cerr << "SRK Section" << endl;
		if(b_log)	clog << "SRK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_TSP_SRK, &srk);
		if(b_debug)	cerr << ' ' << result << " create srk object" << endl;
		if(b_log)	cerr << ' ' << result << " create srk object" << endl;

	result = Tspi_Context_LoadKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_srk, &srk);
		if(b_debug)	cerr << ' ' << result << " load srk by uuid" << endl;
		if(b_log)	cerr << ' ' << result << " load srk by uuid" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_srk);
		if(b_debug)	cerr << ' ' << result << " create srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " create srk policy" << endl;

	result = Tspi_Policy_SetSecret(policy_srk, TSS_SECRET_MODE_PLAIN, wks_size, wks_blob);
		if(b_debug)	cerr << ' ' << result << " set srk auth" << endl;
		if(b_log)	cerr << ' ' << result << " set srk auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_srk, srk);
		if(b_debug)	cerr << ' ' << result << " assign srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " assign srk policy" << endl;


//KEY OPERATIONS (NOT SET YET)
	TSS_HKEY		key;
	TSS_HPOLICY		policy_key;
	UINT32			init_flags;

		if(b_debug)	cerr << "Key Section" << endl;
		if(b_log)	clog << "Key Section" << endl;

	if(keytype.compare("bind") == 0)
	{
		//uuid_key.rgbNode[5] = 0x05;
		uuid_key.rgbNode[0] = 0x05;

		if(!b_1024)
			init_flags = TSS_KEY_TYPE_BIND | TSS_KEY_SIZE_2048  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;
		else
			init_flags = TSS_KEY_TYPE_BIND | TSS_KEY_SIZE_1024  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;

			if(b_debug)	cerr << " binding key selected" << endl;
			if(b_log)	cerr << " binding key selected" << endl;
	}
	else if(keytype.compare("sign") == 0)
	{
		//uuid_key.rgbNode[5] = 0x06;
		uuid_key.rgbNode[0] = 0x06;

		if(!b_1024)
			init_flags = TSS_KEY_TYPE_SIGNING | TSS_KEY_SIZE_2048  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;
		else
			init_flags = TSS_KEY_TYPE_SIGNING | TSS_KEY_SIZE_1024  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;

			if(b_debug)	cerr << " signing key selected" << endl;
			if(b_log)	cerr << " signing key selected" << endl;
	}

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, init_flags, &key);
		if(b_debug)	cerr << ' ' << result << " create key object" << endl;
		if(b_log)	cerr << ' ' << result << " create key object" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_key);
		if(b_debug)	cerr << ' ' << result << " create key policy" << endl;
		if(b_log)	cerr << ' ' << result << " create key policy" << endl;

	result = Tspi_Policy_SetSecret(policy_key, TSS_SECRET_MODE_PLAIN, keyauth.size, keyauth.blob);
		if(b_debug)	cerr << ' ' << result << " set key auth" << endl;
		if(b_log)	cerr << ' ' << result << " set key auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_key, key);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;

	if(b_pkcs)
	{
		result = Tspi_SetAttribUint32(key, TSS_TSPATTRIB_KEY_INFO, TSS_TSPATTRIB_KEYINFO_ENCSCHEME, TSS_ES_RSAESPKCSV15);
			if(b_debug)	cerr << ' ' << result << " set encryption scheme to PKCS" << endl;
			if(b_log)	cerr << ' ' << result << " set encryption scheme to PKCS" << endl;
	}


//CREATE KEY
	UINT32			mod_size;
	UINT32			blob_size;
	BYTE*			mod_blob;
	BYTE*			blob_blob;

		if(b_debug)	cerr << "Create Key Section" << endl;
		if(b_log)	clog << "Create Key Section" << endl;

	result = Tspi_Key_CreateKey(key, srk, NULL);
		if(b_debug)	cerr << ' ' << result << " CREATE KEY" << endl;
		if(b_log)	cerr << ' ' << result << " CREATE KEY" << endl;

	result = Tspi_GetAttribData(key, TSS_TSPATTRIB_RSAKEY_INFO, TSS_TSPATTRIB_KEYINFO_RSA_MODULUS, &mod_size, &mod_blob);
		if(b_debug)	cerr << ' ' << result << " get modulus" << endl;
		if(b_log)	cerr << ' ' << result << " get modulus" << endl;

	if(result == 0)
	{
		for(UINT32 i = 0; i < mod_size; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)mod_blob[i];
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		result = Tspi_Context_FreeMemory(context, mod_blob);
	}

	result = Tspi_GetAttribData(key, TSS_TSPATTRIB_KEY_BLOB, TSS_TSPATTRIB_KEYBLOB_BLOB, &blob_size, &blob_blob);
		if(b_debug)	cerr << ' ' << result << " get key blob" << endl;
		if(b_log)	cerr << ' ' << result << " get key blob" << endl;

	if(result == 0)
	{
		if(!b_debug && !b_log) if(!b_debug && !b_log) cout << ' ';

		for(UINT32 i = 0; i < blob_size; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)blob_blob[i];
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		result = Tspi_Context_FreeMemory(context, blob_blob);
	}


//SAVE THE KEY
	result = Tspi_Key_LoadKey(key, srk);
		if(b_debug)	cerr << ' ' << result << " load the new key" << endl;
		if(b_log)	cerr << ' ' << result << " load the new key" << endl;

	result = Tspi_Context_RegisterKey(context, key, TSS_PS_TYPE_SYSTEM, uuid_key, TSS_PS_TYPE_SYSTEM, uuid_srk);
		if(b_debug)	cerr << ' ' << result << " register new key" << endl;
		if(b_log)	cerr << ' ' << result << " register new key" << endl;
		return_code = result;


//CLEANUP SECTION
	result = Tspi_Context_CloseObject(context, policy_key);
	result = Tspi_Context_CloseObject(context, key);
	result = Tspi_Context_CloseObject(context, srk);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Set Key
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::set_key()
{
	if(b_help)
	{
		cout << "Set Key (" << i_mode << ") --- Creates and stores (no overwrite) a signing, binding, or identity via a key blob" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -key_type (string, sign or bind or identity)" << endl;
		cout << " -key_auth (hex blob, key authorization data)" << endl;
		cout << " -key_blob (hex blob, key blob)" << endl;
		cout << " -key_index (integer, index number for key)" << endl;
		cout << " OPTIONAL PARAMETERS" << endl;
		cout << " -1024 (flag, uses 1024 keys instead of 2048)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	string				keytype;
	string				s_keyauth;
	string				s_keyblob;
	int					i_keyindex = 0;
	bool				b_1024 = false;
	bool				b_pkcs = false;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-1024") == 0)
		{
			b_1024 = true;
		}

		if(s_argv[i].compare("-pkcs") == 0)
		{
			b_pkcs = true;
		}

		if(s_argv[i].compare("-key_type") == 0)
		{
			if(++i >= i_argc) return;
			keytype = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_keyauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_blob") == 0)
		{
			if(++i >= i_argc) return;
			s_keyblob = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_index") == 0)
		{
			if(++i >= i_argc) return;
			i_keyindex = atoi(s_argv[i].c_str());
			i_success++;
		}
	}
	if(i_success != 4)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	keyauth(s_keyauth);
	NIARL_Util_ByteBlob	keyblob(s_keyblob);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;
	UINT32				wks_size = sizeof(wks_blob);

	TSS_UUID			uuid_key = TSS_UUID_USK2;
	uuid_key.rgbNode[5] = (BYTE)i_keyindex;


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;


//SRK OPERATIONS (SET)
	TSS_HKEY		srk;
	TSS_HPOLICY		policy_srk;
	TSS_UUID		uuid_srk = TSS_UUID_SRK;

		if(b_debug)	cerr << "SRK Section" << endl;
		if(b_log)	clog << "SRK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_TSP_SRK, &srk);
		if(b_debug)	cerr << ' ' << result << " create srk object" << endl;
		if(b_log)	cerr << ' ' << result << " create srk object" << endl;

	result = Tspi_Context_LoadKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_srk, &srk);
		if(b_debug)	cerr << ' ' << result << " load srk by uuid" << endl;
		if(b_log)	cerr << ' ' << result << " load srk by uuid" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_srk);
		if(b_debug)	cerr << ' ' << result << " create srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " create srk policy" << endl;

	result = Tspi_Policy_SetSecret(policy_srk, TSS_SECRET_MODE_PLAIN, wks_size, wks_blob);
		if(b_debug)	cerr << ' ' << result << " set srk auth" << endl;
		if(b_log)	cerr << ' ' << result << " set srk auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_srk, srk);
		if(b_debug)	cerr << ' ' << result << " assign srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " assign srk policy" << endl;


//KEY OPERATIONS (NOT SET YET)
	TSS_HKEY		key;
	TSS_HPOLICY		policy_key;
	UINT32			init_flags;

		if(b_debug)	cerr << "Key Section" << endl;
		if(b_log)	clog << "Key Section" << endl;

	if(keytype.compare("identity") == 0)
	{
		//uuid_key.rgbNode[5] = 0x04;
		uuid_key.rgbNode[0] = 0x04;
		init_flags = TSS_KEY_TYPE_IDENTITY | TSS_KEY_SIZE_2048  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;
			if(b_debug)	cerr << "aik selected" << endl;
			if(b_log)	clog << "aik selected" << endl;
	}
	else if(keytype.compare("bind") == 0)
	{
		//uuid_key.rgbNode[5] = 0x05;
		uuid_key.rgbNode[0] = 0x05;

		if(!b_1024)
			init_flags = TSS_KEY_TYPE_BIND | TSS_KEY_SIZE_2048  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;
		else
			init_flags = TSS_KEY_TYPE_BIND | TSS_KEY_SIZE_1024  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;

			if(b_debug)	cerr << " binding key selected" << endl;
			if(b_log)	cerr << " binding key selected" << endl;
	}
	else if(keytype.compare("sign") == 0)
	{
		//uuid_key.rgbNode[5] = 0x06;
		uuid_key.rgbNode[0] = 0x06;

		if(!b_1024)
			init_flags = TSS_KEY_TYPE_SIGNING | TSS_KEY_SIZE_2048  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;
		else
			init_flags = TSS_KEY_TYPE_SIGNING | TSS_KEY_SIZE_1024  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;

			if(b_debug)	cerr << " signing key selected" << endl;
			if(b_log)	cerr << " signing key selected" << endl;
	}

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, init_flags, &key);
		if(b_debug)	cerr << ' ' << result << " create key object" << endl;
		if(b_log)	cerr << ' ' << result << " create key object" << endl;

	result = Tspi_SetAttribData(key, TSS_TSPATTRIB_KEY_BLOB, TSS_TSPATTRIB_KEYBLOB_BLOB, keyblob.size, keyblob.blob);
		if(b_debug)	cerr << ' ' << result << " set key blob" << endl;
		if(b_log)	cerr << ' ' << result << " set key blob" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_key);
		if(b_debug)	cerr << ' ' << result << " create key policy" << endl;
		if(b_log)	cerr << ' ' << result << " create key policy" << endl;

	result = Tspi_Policy_SetSecret(policy_key, TSS_SECRET_MODE_PLAIN, keyauth.size, keyauth.blob);
		if(b_debug)	cerr << ' ' << result << " set key auth" << endl;
		if(b_log)	cerr << ' ' << result << " set key auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_key, key);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;

	if(b_pkcs)
	{
		result = Tspi_SetAttribUint32(key, TSS_TSPATTRIB_KEY_INFO, TSS_TSPATTRIB_KEYINFO_ENCSCHEME, TSS_ES_RSAESPKCSV15);
			if(b_debug)	cerr << ' ' << result << " set encryption scheme to PKCS" << endl;
			if(b_log)	cerr << ' ' << result << " set encryption scheme to PKCS" << endl;
	}


//SAVE THE KEY
	result = Tspi_Key_LoadKey(key, srk);
		if(b_debug)	cerr << ' ' << result << " load the new key" << endl;
		if(b_log)	cerr << ' ' << result << " load the new key" << endl;

	result = Tspi_Context_RegisterKey(context, key, TSS_PS_TYPE_SYSTEM, uuid_key, TSS_PS_TYPE_SYSTEM, uuid_srk);
		if(b_debug)	cerr << ' ' << result << " register new key" << endl;
		if(b_log)	cerr << ' ' << result << " register new key" << endl;
		return_code = result;


//CLEANUP SECTION
	result = Tspi_Context_CloseObject(context, policy_key);
	result = Tspi_Context_CloseObject(context, key);
	result = Tspi_Context_CloseObject(context, srk);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Get Key
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::get_key()
{
	if(b_help)
	{
		cout << "Get Key (" << i_mode << ") --- Gets the modulus and blob of an identity, signing, or binding key" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -key_type (string, sign or bind or identity or ek)" << endl;
		cout << " EK PARAMETERS" << endl;
		cout << " -owner_auth (hex blob, owner authorization)" << endl;
		cout << " -nonce (hex blob, anti-replay nonce, REQUIRED FOR EK)" << endl;
		cout << " BIND, SIGN, AND AIK PARAMETERS" << endl;
		cout << " -key_index (integer, index number for key, REQUIRED FOR KEY)" << endl;
		cout << " -key_auth (hex blob, key authorization data, owner auth for ek)" << endl;
		cout << " OPTIONAL PARAMETERS FOR BIND AND SIGN" << endl;
		cout << " -1024 (flag, uses 1024 keys instead of 2048)" << endl;
		cout << " OUTPUTS" << endl;
		cout << " modulus (hex blob, key modulus)" << endl;
		cout << " key blob (hex blob, key blob)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	string				s_ownerauth;
	string				keytype;
	string				s_keyauth;
	string				s_nonce;
	int					i_keyindex = 0;
	bool				b_1024 = false;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-1024") == 0)
		{
			b_1024 = true;
		}

		if(s_argv[i].compare("-owner_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_ownerauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_type") == 0)
		{
			if(++i >= i_argc) return;
			keytype = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_keyauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-nonce") == 0)
		{
			if(++i >= i_argc) return;
			s_nonce = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_index") == 0)
		{
			if(++i >= i_argc) return;
			i_keyindex = atoi(s_argv[i].c_str());
			i_success++;
		}
	}
	if(i_success < 3)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	ownerauth(s_ownerauth);
	NIARL_Util_ByteBlob	keyauth(s_keyauth);
	NIARL_Util_ByteBlob	nonce(s_nonce);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;
	UINT32				wks_size = sizeof(wks_blob);

	TSS_UUID			uuid_key = TSS_UUID_USK2;
	uuid_key.rgbNode[5] = (BYTE)i_keyindex;


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;
	TSS_HPOLICY		policy_tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;

	if(keytype.compare("ek") == 0)
	{ //ek only commands
		result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_tpm);
			if(b_debug)	cerr << ' ' << result << " create policy" << endl;
			if(b_log)	cerr << ' ' << result << " create policy" << endl;

		result = Tspi_Policy_SetSecret(policy_tpm, TSS_SECRET_MODE_PLAIN, ownerauth.size, ownerauth.blob);
			if(b_debug)	cerr << ' ' << result << " owner auth" << endl;
			if(b_log)	cerr << ' ' << result << " owner auth" << endl;

		result = Tspi_Policy_AssignToObject(policy_tpm, tpm);
			if(b_debug)	cerr << ' ' << result << " assign" << endl;
			if(b_log)	cerr << ' ' << result << " assign" << endl;
	}


//SRK OPERATIONS (SET)
	TSS_HKEY		srk;
	TSS_HPOLICY		policy_srk;
	TSS_UUID		uuid_srk = TSS_UUID_SRK;

		if(b_debug)	cerr << "SRK Section" << endl;
		if(b_log)	clog << "SRK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_TSP_SRK, &srk);
		if(b_debug)	cerr << ' ' << result << " create srk object" << endl;
		if(b_log)	cerr << ' ' << result << " create srk object" << endl;

	result = Tspi_Context_LoadKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_srk, &srk);
		if(b_debug)	cerr << ' ' << result << " load srk by uuid" << endl;
		if(b_log)	cerr << ' ' << result << " load srk by uuid" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_srk);
		if(b_debug)	cerr << ' ' << result << " create srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " create srk policy" << endl;

	result = Tspi_Policy_SetSecret(policy_srk, TSS_SECRET_MODE_PLAIN, wks_size, wks_blob);
		if(b_debug)	cerr << ' ' << result << " set srk auth" << endl;
		if(b_log)	cerr << ' ' << result << " set srk auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_srk, srk);
		if(b_debug)	cerr << ' ' << result << " assign srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " assign srk policy" << endl;


//KEY OPERATIONS (SET)
	TSS_VALIDATION	validation;
	TSS_HKEY		key;
	TSS_HPOLICY		policy_key;
	UINT32			init_flags;

		if(b_debug)	cerr << "Key Section" << endl;
		if(b_log)	clog << "Key Section" << endl;

	if(keytype.compare("identity") == 0)
	{
		//uuid_key.rgbNode[5] = 0x04;
		uuid_key.rgbNode[0] = 0x04;
		init_flags = TSS_KEY_TYPE_IDENTITY | TSS_KEY_SIZE_2048  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;
			if(b_debug)	cerr << "aik selected" << endl;
			if(b_log)	clog << "aik selected" << endl;
	}
	else if(keytype.compare("bind") == 0)
	{
		//uuid_key.rgbNode[5] = 0x05;
		uuid_key.rgbNode[0] = 0x05;

		if(!b_1024)
			init_flags = TSS_KEY_TYPE_BIND | TSS_KEY_SIZE_2048  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;
		else
			init_flags = TSS_KEY_TYPE_BIND | TSS_KEY_SIZE_1024  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;

			if(b_debug)	cerr << "binding key selected" << endl;
			if(b_log)	clog << "binding key selected" << endl;
	}
	else if(keytype.compare("sign") == 0)
	{
		//uuid_key.rgbNode[5] = 0x06;
		uuid_key.rgbNode[0] = 0x06;

		if(!b_1024)
			init_flags = TSS_KEY_TYPE_SIGNING | TSS_KEY_SIZE_2048  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;
		else
			init_flags = TSS_KEY_TYPE_SIGNING | TSS_KEY_SIZE_1024  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;

			if(b_debug)	cerr << "signing key selected" << endl;
			if(b_log)	clog << "signing key selected" << endl;
	}

	if(keytype.compare("ek") == 0)
	{ //ek only commands
		memset(&validation, 0, sizeof(TSS_VALIDATION));
		validation.versionInfo.bMajor = 0x01;
		validation.versionInfo.bMinor = 0x02;
		validation.versionInfo.bRevMajor = 0x01;
		validation.versionInfo.bRevMinor = 0x25;
		validation.ulExternalDataLength = sizeof(TSS_NONCE);
		validation.rgbExternalData = nonce.blob;
		result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_SIZE_DEFAULT, &key);
			if(b_debug)	cerr << ' ' << result << " create ek object" << endl;
			if(b_log)	cerr << ' ' << result << " create ek object" << endl;

		result = Tspi_Policy_AssignToObject(policy_tpm, key);
			if(b_debug)	cerr << ' ' << result << " assign owner auth to ek" << endl;
			if(b_log)	cerr << ' ' << result << " assign owner auth to ek" << endl;

		result = Tspi_TPM_GetPubEndorsementKey(tpm, true, &validation, &key);
			if(b_debug)	cerr << ' ' << result << " get public ek" << endl;
			if(b_log)	cerr << ' ' << result << " get public ek" << endl;
	}
	else
	{ //commands for aik, signing, and binding keys
		result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, init_flags, &key);
			if(b_debug)	cerr << ' ' << result << " create key object" << endl;
			if(b_log)	cerr << ' ' << result << " create key object" << endl;

		result = Tspi_Context_GetKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_key, &key);
			if(b_debug)	cerr << ' ' << result << " load key by uuid" << endl;
			if(b_log)	cerr << ' ' << result << " load key by uuid" << endl;

		result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_key);
			if(b_debug)	cerr << ' ' << result << " create key policy" << endl;
			if(b_log)	cerr << ' ' << result << " create key policy" << endl;

		result = Tspi_Policy_SetSecret(policy_key, TSS_SECRET_MODE_PLAIN, keyauth.size, keyauth.blob);
			if(b_debug)	cerr << ' ' << result << " set key auth" << endl;
			if(b_log)	cerr << ' ' << result << " set key auth" << endl;

		result = Tspi_Policy_AssignToObject(policy_key, key);
			if(b_debug)	cerr << ' ' << result << " assign" << endl;
			if(b_log)	cerr << ' ' << result << " assign" << endl;

		result = Tspi_Key_LoadKey(key, srk);
			if(b_debug)	cerr << ' ' << result << " load key" << endl;
			if(b_log)	cerr << ' ' << result << " load key" << endl;
	}


//GET KEY
	UINT32			mod_size;
	UINT32			blob_size;
	BYTE*			mod_blob;
	BYTE*			blob_blob;

		if(b_debug)	cerr << "Get Key Section" << endl;
		if(b_log)	clog << "Get Key Section" << endl;

	result = Tspi_GetAttribData(key, TSS_TSPATTRIB_RSAKEY_INFO, TSS_TSPATTRIB_KEYINFO_RSA_MODULUS, &mod_size, &mod_blob);
		if(b_debug)	cerr << ' ' << result << " get modulus" << endl;
		if(b_log)	cerr << ' ' << result << " get modulus" << endl;
		return_code = result;

	if(result == 0)
	{
		for(UINT32 i = 0; i < mod_size; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)mod_blob[i];

		if(!b_debug && !b_log) cout << ' ';
		if(b_debug)	cerr << endl;
		if(b_log)	cerr << endl;

		result = Tspi_Context_FreeMemory(context, mod_blob);
	}

	if(keytype.compare("ek") != 0)
	{
		result = Tspi_GetAttribData(key, TSS_TSPATTRIB_KEY_BLOB, TSS_TSPATTRIB_KEYBLOB_BLOB, &blob_size, &blob_blob);
			if(b_debug)	cerr << ' ' << result << " get key blob" << endl;
			if(b_log)	cerr << ' ' << result << " get key blob" << endl;
			return_code = result;

		if(result == 0)
		{
			for(UINT32 i = 0; i < blob_size; i++)
				cout << setw(2) << setfill('0') << setbase(16) << (int)blob_blob[i];
			if(b_debug) cerr << endl;
			if(b_log) clog << endl;

			result = Tspi_Context_FreeMemory(context, blob_blob);
		}
	}


//CLEANUP SECTION
	result = Tspi_Context_CloseObject(context, policy_tpm);
	result = Tspi_Context_CloseObject(context, policy_key);
	result = Tspi_Context_CloseObject(context, key);
	result = Tspi_Context_CloseObject(context, srk);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Clear Key
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::clear_key()
{
	if(b_help)
	{
		cout << "Clear Key (" << i_mode << ") --- Clears an existing identity, signing, or binding key" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -key_type (string, sign or bind or identity)" << endl;
		cout << " -key_auth (hex blob, key authorization data)" << endl;
		cout << " -key_index (integer, index number for key)" << endl;
		cout << " OPTIONAL PARAMETERS" << endl;
		cout << " -1024 (flag, uses 1024 keys instead of 2048)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	string				keytype;
	string				s_keyauth;
	int					i_keyindex = 0;
	bool				b_1024 = false;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-1024") == 0)
		{
			b_1024 = true;
		}

		if(s_argv[i].compare("-key_type") == 0)
		{
			if(++i >= i_argc) return;
			keytype = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_keyauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_index") == 0)
		{
			if(++i >= i_argc) return;
			i_keyindex = atoi(s_argv[i].c_str());
			i_success++;
		}
	}
	if(i_success != 3)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	keyauth(s_keyauth);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;

	UINT32				wks_size = sizeof(wks_blob);

	TSS_UUID			uuid_key = TSS_UUID_USK2;
	uuid_key.rgbNode[5] = (BYTE)i_keyindex;


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;


//SRK OPERATIONS (SET)
	TSS_HKEY		srk;
	TSS_HPOLICY		policy_srk;
	TSS_UUID		uuid_srk = TSS_UUID_SRK;

		if(b_debug)	cerr << "SRK Section" << endl;
		if(b_log)	clog << "SRK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_TSP_SRK, &srk);
		if(b_debug)	cerr << ' ' << result << " create srk object" << endl;
		if(b_log)	cerr << ' ' << result << " create srk object" << endl;

	result = Tspi_Context_LoadKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_srk, &srk);
		if(b_debug)	cerr << ' ' << result << " load srk by uuid" << endl;
		if(b_log)	cerr << ' ' << result << " load srk by uuid" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_srk);
		if(b_debug)	cerr << ' ' << result << " create srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " create srk policy" << endl;

	result = Tspi_Policy_SetSecret(policy_srk, TSS_SECRET_MODE_PLAIN, wks_size, wks_blob);
		if(b_debug)	cerr << ' ' << result << " set srk auth" << endl;
		if(b_log)	cerr << ' ' << result << " set srk auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_srk, srk);
		if(b_debug)	cerr << ' ' << result << " assign srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " assign srk policy" << endl;


//KEY OPERATIONS (SET)
	TSS_HKEY		key;
	TSS_HPOLICY		policy_key;
	UINT32			init_flags;

		if(b_debug)	cerr << "Key Section" << endl;
		if(b_log)	clog << "Key Section" << endl;

	if(keytype.compare("identity") == 0)
	{
		//uuid_key.rgbNode[5] = 0x04;
		uuid_key.rgbNode[0] = 0x04;
		init_flags = TSS_KEY_TYPE_IDENTITY | TSS_KEY_SIZE_2048  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;
			if(b_debug)	cerr << "aik selected" << endl;
			if(b_log)	clog << "aik selected" << endl;
	}
	else if(keytype.compare("bind") == 0)
	{
		//uuid_key.rgbNode[5] = 0x05;
		uuid_key.rgbNode[0] = 0x05;

		if(!b_1024)
			init_flags = TSS_KEY_TYPE_BIND | TSS_KEY_SIZE_2048  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;
		else
			init_flags = TSS_KEY_TYPE_BIND | TSS_KEY_SIZE_1024  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;

			if(b_debug)	cerr << "binding key selected" << endl;
			if(b_log)	clog << "binding key selected" << endl;
	}
	else if(keytype.compare("sign") == 0)
	{
		//uuid_key.rgbNode[5] = 0x06;
		uuid_key.rgbNode[0] = 0x06;

		if(!b_1024)
			init_flags = TSS_KEY_TYPE_SIGNING | TSS_KEY_SIZE_2048  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;
		else
			init_flags = TSS_KEY_TYPE_SIGNING | TSS_KEY_SIZE_1024  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;

			if(b_debug)	cerr << "signing key selected" << endl;
			if(b_log)	clog << "signing key selected" << endl;
	}

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, init_flags, &key);
		if(b_debug)	cerr << ' ' << result << " create key object" << endl;
		if(b_log)	cerr << ' ' << result << " create key object" << endl;

	result = Tspi_Context_GetKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_key, &key);
		if(b_debug)	cerr << ' ' << result << " load key by uuid" << endl;
		if(b_log)	cerr << ' ' << result << " load key by uuid" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_key);
		if(b_debug)	cerr << ' ' << result << " create key policy" << endl;
		if(b_log)	cerr << ' ' << result << " create key policy" << endl;

	result = Tspi_Policy_SetSecret(policy_key, TSS_SECRET_MODE_PLAIN, keyauth.size, keyauth.blob);
		if(b_debug)	cerr << ' ' << result << " set key auth" << endl;
		if(b_log)	cerr << ' ' << result << " set key auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_key, key);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;

	result = Tspi_Key_LoadKey(key, srk);
		if(b_debug)	cerr << ' ' << result << " load key" << endl;
		if(b_log)	cerr << ' ' << result << " load key" << endl;


//CLEAR KEY
	TSS_HKEY		key_blank;

		if(b_debug)	cerr << "Clear Key Section" << endl;
		if(b_log)	clog << "Clear Key Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_SIZE_DEFAULT, &key_blank);
		if(b_debug)	cerr << ' ' << result << " create blank key" << endl;
		if(b_log)	cerr << ' ' << result << " create blank key" << endl;

	result = Tspi_Context_UnregisterKey(context, TSS_PS_TYPE_SYSTEM, uuid_key, &key_blank);
		if(b_debug)	cerr << ' ' << result << " UNREGISTER" << endl;
		if(b_log)	cerr << ' ' << result << " UNREGISTER" << endl;
		return_code = result;


//CLEANUP SECTION
	result = Tspi_Context_CloseObject(context, policy_key);
	result = Tspi_Context_CloseObject(context, key);
	result = Tspi_Context_CloseObject(context, key_blank);
	result = Tspi_Context_CloseObject(context, srk);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Set Credential
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::set_credential()
{
	if(b_help)
	{
		cout << "Set Credential (" << i_mode << ") --- Sets a TPM credential (no overwrite)" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -owner_auth (hex blob, owner authorization)" << endl;
		cout << " -cred_type (string, EC or CC or PC or PCC)" << endl;
		cout << " -blob (hex blob, credential)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	string				s_ownerauth;
	string				s_credtype;
	string				s_blob;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-owner_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_ownerauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-cred_type") == 0)
		{
			if(++i >= i_argc) return;
			s_credtype = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-blob") == 0)
		{
			if(++i >= i_argc) return;
			s_blob = s_argv[i];
			i_success++;
		}
	}
	if(i_success != 3)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	ownerauth(s_ownerauth);
	NIARL_Util_ByteBlob	credential(s_blob);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;
	UINT32				wks_size = sizeof(wks_blob);


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;
	TSS_HPOLICY		policy_tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_tpm);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Policy_SetSecret(policy_tpm, TSS_SECRET_MODE_PLAIN, ownerauth.size, ownerauth.blob);
		if(b_debug)	cerr << ' ' << result << " owner auth" << endl;
		if(b_log)	cerr << ' ' << result << " owner auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_tpm, tpm);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;


//NVSTORE SECTION
	TSS_HNVSTORE	nvstore;

		if(b_debug)	cerr << "NVStore Section" << endl;
		if(b_log)	clog << "NVStore Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_NV, NULL, &nvstore);
		if(b_debug)	cerr << ' ' << result << " create nvstore object" << endl;
		if(b_log)	cerr << ' ' << result << " create nvstore object" << endl;

	result = Tspi_Policy_AssignToObject(policy_tpm, nvstore);
		if(b_debug)	cerr << ' ' << result << " assign owner auth" << endl;
		if(b_log)	cerr << ' ' << result << " assign owner auth" << endl;

	if(s_credtype.compare("EC") == 0)
	{
		result = Tspi_SetAttribUint32(nvstore, TSS_TSPATTRIB_NV_INDEX, NULL, TPM_NV_INDEX_EKCert);
			if(b_debug)	cerr << " EK cert selected" << endl;
			if(b_log)	clog << " EK cert selected" << endl;
	}

	else if(s_credtype.compare("CC") == 0)
	{
		result = Tspi_SetAttribUint32(nvstore, TSS_TSPATTRIB_NV_INDEX, NULL, TPM_NV_INDEX_TPM_CC);
			if(b_debug)	cerr << " Conformance cert selected" << endl;
			if(b_log)	clog << " Conformance cert selected" << endl;
	}

	else if(s_credtype.compare("PC") == 0)
	{
		result = Tspi_SetAttribUint32(nvstore, TSS_TSPATTRIB_NV_INDEX, NULL, TPM_NV_INDEX_PlatformCert);
			if(b_debug)	cerr << " Platform cert selected" << endl;
			if(b_log)	clog << " Platform cert selected" << endl;
	}

	else if(s_credtype.compare("PCC") == 0)
	{
		result = Tspi_SetAttribUint32(nvstore, TSS_TSPATTRIB_NV_INDEX, NULL, TPM_NV_INDEX_Platform_CC);
			if(b_debug)	cerr << "Platform Conformance cert selected" << endl;
			if(b_log)	clog << "Platform Conformance cert selected" << endl;
	}

	result = Tspi_SetAttribUint32(nvstore, TSS_TSPATTRIB_NV_PERMISSIONS, NULL, TPM_NV_PER_OWNERREAD | TPM_NV_PER_OWNERWRITE);
		if(b_debug)	cerr << ' ' << result << " set nvstore permissions" << endl;
		if(b_log)	cerr << ' ' << result << " set nvstore permissions" << endl;

	result = Tspi_SetAttribUint32(nvstore, TSS_TSPATTRIB_NV_DATASIZE, NULL, credential.size);
		if(b_debug)	cerr << ' ' << result << " set nvstore size" << endl;
		if(b_log)	cerr << ' ' << result << " set nvstore size" << endl;

	result = Tspi_NV_DefineSpace(nvstore, NULL, NULL);
		if(b_debug)	cerr << ' ' << result << " define space" << endl;
		if(b_log)	cerr << ' ' << result << " define space" << endl;

	result = Tspi_NV_WriteValue(nvstore, 0, credential.size, credential.blob);
		if(b_debug)	cerr << ' ' << result << " nv write" << endl;
		if(b_log)	cerr << ' ' << result << " nv write" << endl;
		return_code = result;


//CLEANUP SECTION
	result = Tspi_Context_CloseObject(context, policy_tpm);
	result = Tspi_Context_CloseObject(context, nvstore);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Get Credential
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::get_credential()
{
	if(b_help)
	{
		cout << "Get Credential (" << i_mode << ") --- Gets an existing TPM credential" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -owner_auth (hex blob, owner authorization)" << endl;
		cout << " -cred_type (string, EC or CC or PC or PCC)" << endl;
		cout << " OPTIONAL PARAMETERS" << endl;
		cout << " -trousers (flag, manually determines credential size from DER x509 size header)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	string				s_ownerauth;
	string				s_credtype;
	bool				b_trousers = false;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-owner_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_ownerauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-cred_type") == 0)
		{
			if(++i >= i_argc) return;
			s_credtype = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-trousers") == 0)
		{
			b_trousers = true;
		}
	}
	if(i_success != 2)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	ownerauth(s_ownerauth);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;
	UINT32				wks_size = sizeof(wks_blob);


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;
	TSS_HPOLICY		policy_tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_tpm);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Policy_SetSecret(policy_tpm, TSS_SECRET_MODE_PLAIN, ownerauth.size, ownerauth.blob);
		if(b_debug)	cerr << ' ' << result << " owner auth" << endl;
		if(b_log)	cerr << ' ' << result << " owner auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_tpm, tpm);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;


//NVSTORE SECTION
	TSS_HNVSTORE	nvstore;
	UINT32			cred_size;
	BYTE*			cred_blob;

		if(b_debug)	cerr << "NVStore Section" << endl;
		if(b_log)	clog << "NVStore Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_NV, NULL, &nvstore);
		if(b_debug)	cerr << ' ' << result << " create nvstore object" << endl;
		if(b_log)	cerr << ' ' << result << " create nvstore object" << endl;

	result = Tspi_Policy_AssignToObject(policy_tpm, nvstore);
		if(b_debug)	cerr << ' ' << result << " assign owner auth" << endl;
		if(b_log)	cerr << ' ' << result << " assign owner auth" << endl;

	if(s_credtype.compare("EC") == 0)
	{
		result = Tspi_SetAttribUint32(nvstore, TSS_TSPATTRIB_NV_INDEX, NULL, TPM_NV_INDEX_EKCert);
			if(b_debug)	cerr << " EK cert selected" << endl;
			if(b_log)	clog << " EK cert selected" << endl;
	}

	else if(s_credtype.compare("CC") == 0)
	{
		result = Tspi_SetAttribUint32(nvstore, TSS_TSPATTRIB_NV_INDEX, NULL, TPM_NV_INDEX_TPM_CC);
			if(b_debug)	cerr << " Conformance cert selected" << endl;
			if(b_log)	clog << " Conformance cert selected" << endl;
	}

	else if(s_credtype.compare("PC") == 0)
	{
		result = Tspi_SetAttribUint32(nvstore, TSS_TSPATTRIB_NV_INDEX, NULL, TPM_NV_INDEX_PlatformCert);
			if(b_debug)	cerr << " Platform cert selected" << endl;
			if(b_log)	clog << " Platform cert selected" << endl;
	}

	else if(s_credtype.compare("PCC") == 0)
	{
		result = Tspi_SetAttribUint32(nvstore, TSS_TSPATTRIB_NV_INDEX, NULL, TPM_NV_INDEX_Platform_CC);
			if(b_debug)	cerr << " Platform Conformance cert selected" << endl;
			if(b_log)	clog << " Platform Conformance cert selected" << endl;
	}

	if(b_trousers)
	{//Trousers mode. Size cannot be automatically determined
		if(b_debug)	cerr << " Trousers mode activated" << endl;
		if(b_log)	clog << " Trousers mode activated" << endl;

		UINT32			counter = 0;

		cred_size = 10;		//allow enough space to get DER x509 size header
		result = Tspi_NV_ReadValue(nvstore, 0, &cred_size, &cred_blob);

		if((int)cred_blob[1] >= 128)	//size is too big for [1]
		{
			counter = (int)cred_blob[1] - 128;
			cred_size = 0;				//reset cred size

			for(UINT32 i = 0; i < counter; i++)
			{
				cred_size *= 256;					//base multiplier
				cred_size += (int)cred_blob[2 + i];	//accumulator
			}
		}
		else
		{
			cred_size = (int)cred_blob[1];
		}

		cred_size += 4;

		if(b_debug)	cerr << " Credential size is " << cred_size << endl;
		if(b_log)	clog << " Credential size is " << cred_size << endl;
	}
	else
	{//NTru mode
		result = Tspi_GetAttribUint32(nvstore, TSS_TSPATTRIB_NV_DATASIZE, NULL, &cred_size);
			if(b_debug)	cerr << ' ' << result << " get nvstore size of " << cred_size << endl;
			if(b_log)	cerr << ' ' << result << " get nvstore size of " << cred_size << endl;
	}

	result = Tspi_NV_ReadValue(nvstore, 0, &cred_size, &cred_blob);
		if(b_debug)	cerr << ' ' << result << " nv read" << endl;
		if(b_log)	cerr << ' ' << result << " nv read" << endl;
		return_code = result;

	if(result == 0)
	{
		for(UINT32 i = 0; i < cred_size; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)cred_blob[i];
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		result = Tspi_Context_FreeMemory(context, cred_blob);
	}


//CLEANUP SECTION
	result = Tspi_Context_CloseObject(context, policy_tpm);
	result = Tspi_Context_CloseObject(context, nvstore);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Clear Credential
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::clear_credential()
{
	if(b_help)
	{
		cout << "Clear Credential (" << i_mode << ") --- Clears an existing TPM credential" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -owner_auth (hex blob, owner authorization)" << endl;
		cout << " -cred_type (string, EC or CC or PC or PCC)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	string				s_ownerauth;
	string				s_credtype;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-owner_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_ownerauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-cred_type") == 0)
		{
			if(++i >= i_argc) return;
			s_credtype = s_argv[i];
			i_success++;
		}
	}
	if(i_success != 2)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	ownerauth(s_ownerauth);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;
	UINT32				wks_size = sizeof(wks_blob);


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;
	TSS_HPOLICY		policy_tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_tpm);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Policy_SetSecret(policy_tpm, TSS_SECRET_MODE_PLAIN, ownerauth.size, ownerauth.blob);
		if(b_debug)	cerr << ' ' << result << " owner auth" << endl;
		if(b_log)	cerr << ' ' << result << " owner auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_tpm, tpm);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;


//NVSTORE SECTION
	TSS_HNVSTORE	nvstore;

		if(b_debug)	cerr << "NVStore Section" << endl;
		if(b_log)	clog << "NVStore Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_NV, NULL, &nvstore);
		if(b_debug)	cerr << ' ' << result << " create nvstore object" << endl;
		if(b_log)	cerr << ' ' << result << " create nvstore object" << endl;

	result = Tspi_Policy_AssignToObject(policy_tpm, nvstore);
		if(b_debug)	cerr << ' ' << result << " assign owner auth" << endl;
		if(b_log)	cerr << ' ' << result << " assign owner auth" << endl;

	if(s_credtype.compare("EC") == 0)
	{
		result = Tspi_SetAttribUint32(nvstore, TSS_TSPATTRIB_NV_INDEX, NULL, TPM_NV_INDEX_EKCert);
			if(b_debug)	cerr << " EK cert selected" << endl;
			if(b_log)	clog << " EK cert selected" << endl;
	}

	else if(s_credtype.compare("CC") == 0)
	{
		result = Tspi_SetAttribUint32(nvstore, TSS_TSPATTRIB_NV_INDEX, NULL, TPM_NV_INDEX_TPM_CC);
			if(b_debug)	cerr << " Conformance cert selected" << endl;
			if(b_log)	clog << " Conformance cert selected" << endl;
	}

	else if(s_credtype.compare("PC") == 0)
	{
		result = Tspi_SetAttribUint32(nvstore, TSS_TSPATTRIB_NV_INDEX, NULL, TPM_NV_INDEX_PlatformCert);
			if(b_debug)	cerr << " Platform cert selected" << endl;
			if(b_log)	clog << " Platform cert selected" << endl;
	}

	else if(s_credtype.compare("PCC") == 0)
	{
		result = Tspi_SetAttribUint32(nvstore, TSS_TSPATTRIB_NV_INDEX, NULL, TPM_NV_INDEX_Platform_CC);
			if(b_debug)	cerr << " Platform Conformance cert selected" << endl;
			if(b_log)	clog << " Platform Conformance cert selected" << endl;
	}

	result = Tspi_NV_ReleaseSpace(nvstore);
		if(b_debug)	cerr << ' ' << result << " nv release" << endl;
		if(b_log)	cerr << ' ' << result << " nv release" << endl;
		return_code = result;


//CLEANUP SECTION
	result = Tspi_Context_CloseObject(context, policy_tpm);
	result = Tspi_Context_CloseObject(context, nvstore);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Seal
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::seal()
{
	if(b_help)
	{
		cout << "Seal (" << i_mode << ") --- Encrypts data based on machine state" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -blob (hex blob, data to encrypt)" << endl;
		cout << " -blob_auth (hex blob, data blob authorization data)" << endl;
		cout << " -mask (hex string, controls PCR index selection)" << endl;
		cout << " OUTPUTS" << endl;
		cout << " sealed data blob (hex blob, encrypted blob)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	string				s_blob;
	string				s_pcrs;
	string				s_encauth;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-blob") == 0)
		{
			if(++i >= i_argc) return;
			s_blob = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-mask") == 0)
		{
			if(++i >= i_argc) return;
			s_pcrs = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-blob_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_encauth = s_argv[i];
			i_success++;
		}
	}
	if(i_success != 3)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	datablob(s_blob);
	NIARL_Util_Mask		pcrmask(s_pcrs);
	NIARL_Util_ByteBlob	encauth(s_encauth);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;
	UINT32				wks_size = sizeof(wks_blob);


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;


//SRK OPERATIONS (SET)
	TSS_HKEY		srk;
	TSS_HPOLICY		policy_srk;
	TSS_UUID		uuid_srk = TSS_UUID_SRK;

		if(b_debug)	cerr << "SRK Section" << endl;
		if(b_log)	clog << "SRK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_TSP_SRK, &srk);
		if(b_debug)	cerr << ' ' << result << " create srk object" << endl;
		if(b_log)	cerr << ' ' << result << " create srk object" << endl;

	result = Tspi_Context_LoadKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_srk, &srk);
		if(b_debug)	cerr << ' ' << result << " load srk by uuid" << endl;
		if(b_log)	cerr << ' ' << result << " load srk by uuid" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_srk);
		if(b_debug)	cerr << ' ' << result << " create srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " create srk policy" << endl;

	result = Tspi_Policy_SetSecret(policy_srk, TSS_SECRET_MODE_PLAIN, wks_size, wks_blob);
		if(b_debug)	cerr << ' ' << result << " set srk auth" << endl;
		if(b_log)	cerr << ' ' << result << " set srk auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_srk, srk);
		if(b_debug)	cerr << ' ' << result << " assign srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " assign srk policy" << endl;


//ENCDATA SECTION
	TSS_HENCDATA		encdata;
	TSS_HPOLICY			policy_encdata;

		if(b_debug)	cerr << "EncData Section" << endl;
		if(b_log)	clog << "EncData Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_ENCDATA, TSS_ENCDATA_SEAL, &encdata);
		if(b_debug)	cerr << ' ' << result << " create encdata" << endl;
		if(b_log)	cerr << ' ' << result << " create encdata" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_encdata);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Policy_SetSecret(policy_encdata, TSS_SECRET_MODE_PLAIN, encauth.size, encauth.blob);
		if(b_debug)	cerr << ' ' << result << " encdata auth" << endl;
		if(b_log)	cerr << ' ' << result << " encdata auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_encdata, encdata);
		if(b_debug)	cerr << ' ' << result << " assign encdata authorization" << endl;
		if(b_log)	cerr << ' ' << result << " assign encdata authorization" << endl;


//PCR OPERATIONS
	TSS_HPCRS		pcr;
	UINT32			pcr_size;
	BYTE*			pcr_blob;

		if(b_debug)	cerr << "PCR Section" << endl;
		if(b_log)	clog << "PCR Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_PCRS, 0, &pcr);
		if(b_debug)	cerr << ' ' << result << " create pcr object" << endl;
		if(b_log)	cerr << ' ' << result << " create pcr object" << endl;

	for(UINT32 i = 0; i < pcrmask.size; i++)
	{
		result = Tspi_TPM_PcrRead(tpm, pcrmask.index[i], &pcr_size, &pcr_blob);
			if(b_debug)	cerr << ' ' << result << " read pcr index " << pcrmask.index[i] << endl;
			if(b_log)	cerr << ' ' << result << " read pcr index " << pcrmask.index[i] << endl;

		result = Tspi_PcrComposite_SelectPcrIndex(pcr, pcrmask.index[i]);
			if(b_debug)	cerr << ' ' << result << " select composite index" << endl;
			if(b_log)	cerr << ' ' << result << " select composite index" << endl;

		result = Tspi_PcrComposite_SetPcrValue(pcr, pcrmask.index[i], pcr_size, pcr_blob);
			if(b_debug)	cerr << ' ' << result << " set pcr value" << endl;
			if(b_log)	cerr << ' ' << result << " set pcr value" << endl;

//		for(UINT32 j = 0; j < pcr_size; j++)
//			cout << setw(2) << setfill('0') << setbase(16) << (int)pcr_blob[j];
//		if(b_debug) cerr << endl;
//		if(b_log) clog << endl;

		result = Tspi_Context_FreeMemory(context, pcr_blob);
			if(b_debug)	cerr << ' ' << result << " clear dynamic array" << endl;
			if(b_log)	cerr << ' ' << result << " clear dynamic array" << endl;
	}


//SEAL OPERATIONS
	UINT32			enc_size;
	BYTE*			enc_blob;

		if(b_debug)	cerr << "Seal Section" << endl;
		if(b_log)	clog << "Seal Section" << endl;

	result = Tspi_Data_Seal(encdata, srk, datablob.size, datablob.blob, pcr);
		if(b_debug)	cerr << ' ' << result << " SEAL" << endl;
		if(b_log)	cerr << ' ' << result << " SEAL" << endl;
		return_code = result;

	if(result == 0)
	{
		result = Tspi_GetAttribData(encdata, TSS_TSPATTRIB_ENCDATA_BLOB, TSS_TSPATTRIB_ENCDATABLOB_BLOB, &enc_size, &enc_blob);
			if(b_debug)	cerr << ' ' << result << " get sealed blob" << endl;
			if(b_log)	cerr << ' ' << result << " get sealed blob" << endl;
			return_code = result;

		if(result == 0)
		{
			for(UINT32 i = 0; i < enc_size; i++)
				cout << setw(2) << setfill('0') << setbase(16) << (int)enc_blob[i];
			if(b_debug) cerr << endl;
			if(b_log) clog << endl;

			result = Tspi_Context_FreeMemory(context, enc_blob);
		}
	}


//CLEANUP SECTION
	result = Tspi_Context_CloseObject(context, policy_encdata);
	result = Tspi_Context_CloseObject(context, encdata);
	result = Tspi_Context_CloseObject(context, srk);
	result = Tspi_Context_CloseObject(context, pcr);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Unseal
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::unseal()
{
	if(b_help)
	{
		cout << "Unseal (" << i_mode << ") --- Decrypts data based on machine state" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -blob (hex blob, data to decrypt)" << endl;
		cout << " -blob_auth (hex blob, data blob authorization data)" << endl;
		cout << " OUTPUTS" << endl;
		cout << " unsealed data blob (hex blob, decrypted data)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	string				s_blob;
	string				s_encauth;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-blob") == 0)
		{
			if(++i >= i_argc) return;
			s_blob = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-blob_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_encauth = s_argv[i];
			i_success++;
		}
	}
	if(i_success != 2)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	datablob(s_blob);
	NIARL_Util_ByteBlob	encauth(s_encauth);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;
	UINT32				wks_size = sizeof(wks_blob);


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;


//SRK OPERATIONS (SET)
	TSS_HKEY		srk;
	TSS_HPOLICY		policy_srk;
	TSS_UUID		uuid_srk = TSS_UUID_SRK;

		if(b_debug)	cerr << "SRK Section" << endl;
		if(b_log)	clog << "SRK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_TSP_SRK, &srk);
		if(b_debug)	cerr << ' ' << result << " create srk object" << endl;
		if(b_log)	cerr << ' ' << result << " create srk object" << endl;

	result = Tspi_Context_LoadKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_srk, &srk);
		if(b_debug)	cerr << ' ' << result << " load srk by uuid" << endl;
		if(b_log)	cerr << ' ' << result << " load srk by uuid" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_srk);
		if(b_debug)	cerr << ' ' << result << " create srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " create srk policy" << endl;

	result = Tspi_Policy_SetSecret(policy_srk, TSS_SECRET_MODE_PLAIN, wks_size, wks_blob);
		if(b_debug)	cerr << ' ' << result << " set srk auth" << endl;
		if(b_log)	cerr << ' ' << result << " set srk auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_srk, srk);
		if(b_debug)	cerr << ' ' << result << " assign srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " assign srk policy" << endl;


//ENCDATA SECTION
	TSS_HENCDATA		encdata;
	TSS_HPOLICY			policy_encdata;

		if(b_debug)	cerr << "EncData Section" << endl;
		if(b_log)	clog << "EncData Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_ENCDATA, TSS_ENCDATA_SEAL, &encdata);
		if(b_debug)	cerr << ' ' << result << " create encdata" << endl;
		if(b_log)	cerr << ' ' << result << " create encdata" << endl;

	result = Tspi_SetAttribData(encdata, TSS_TSPATTRIB_ENCDATA_BLOB, TSS_TSPATTRIB_ENCDATABLOB_BLOB, datablob.size, datablob.blob);
		if(b_debug)	cerr << ' ' << result << " load data blob" << endl;
		if(b_log)	cerr << ' ' << result << " load data blob" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_encdata);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Policy_SetSecret(policy_encdata, TSS_SECRET_MODE_PLAIN, encauth.size, encauth.blob);
		if(b_debug)	cerr << ' ' << result << " encdata auth" << endl;
		if(b_log)	cerr << ' ' << result << " encdata auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_encdata, encdata);
		if(b_debug)	cerr << ' ' << result << " assign encdata authorization" << endl;
		if(b_log)	cerr << ' ' << result << " assign encdata authorization" << endl;


//UNSEAL OPERATIONS
	UINT32			enc_size;
	BYTE*			enc_blob;

		if(b_debug)	cerr << "Unseal Section" << endl;
		if(b_log)	clog << "Unseal Section" << endl;

	result = Tspi_Data_Unseal(encdata, srk, &enc_size, &enc_blob);
		if(b_debug)	cerr << ' ' << result << " UNSEAL" << endl;
		if(b_log)	cerr << ' ' << result << " UNSEAL" << endl;
		return_code = result;

	if(result == 0)
	{
		for(UINT32 i = 0; i < enc_size; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)enc_blob[i];
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		result = Tspi_Context_FreeMemory(context, enc_blob);
	}


//CLEANUP SECTION
	result = Tspi_Context_CloseObject(context, policy_encdata);
	result = Tspi_Context_CloseObject(context, encdata);
	result = Tspi_Context_CloseObject(context, srk);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Bind
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::bind()
{
	if(b_help)
	{
		cout << "Bind (" << i_mode << ") --- Encrypts data based on a binding key" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -blob (hex blob, data to encrypt)" << endl;
		cout << " -blob_auth (hex blob, data blob authorization data)" << endl;
		cout << " -key_auth (hex blob, binding key authorization data)" << endl;
		cout << " -key_index (integer, index number for key)" << endl;
		cout << " OPTIONAL PARAMETERS" << endl;
		cout << " -1024 (flag, uses 1024 keys instead of 2048)" << endl;
		cout << " OUTPUTS" << endl;
		cout << " binded data blob (hex blob, encrypted data)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	string				s_blob;
	string				s_keyauth;
	string				s_encauth;
	int					i_keyindex = 0;
	bool				b_1024 = false;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-1024") == 0)
		{
			b_1024 = true;
		}

		if(s_argv[i].compare("-blob") == 0)
		{
			if(++i >= i_argc) return;
			s_blob = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_keyauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_index") == 0)
		{
			if(++i >= i_argc) return;
			i_keyindex = atoi(s_argv[i].c_str());
			i_success++;
		}

		if(s_argv[i].compare("-blob_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_encauth = s_argv[i];
			i_success++;
		}
	}
	if(i_success != 4)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	datablob(s_blob);
	NIARL_Util_ByteBlob	bindauth(s_keyauth);
	NIARL_Util_ByteBlob	encauth(s_encauth);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;
	UINT32				wks_size = sizeof(wks_blob);

	TSS_UUID			uuid_bind = TSS_UUID_USK2;
	uuid_bind.rgbNode[5] = (BYTE)i_keyindex;
	uuid_bind.rgbNode[0] = 0x05;


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;


//SRK OPERATIONS (SET)
	TSS_HKEY		srk;
	TSS_HPOLICY		policy_srk;
	TSS_UUID		uuid_srk = TSS_UUID_SRK;

		if(b_debug)	cerr << "SRK Section" << endl;
		if(b_log)	clog << "SRK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_TSP_SRK, &srk);
		if(b_debug)	cerr << ' ' << result << " create srk object" << endl;
		if(b_log)	cerr << ' ' << result << " create srk object" << endl;

	result = Tspi_Context_LoadKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_srk, &srk);
		if(b_debug)	cerr << ' ' << result << " load srk by uuid" << endl;
		if(b_log)	cerr << ' ' << result << " load srk by uuid" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_srk);
		if(b_debug)	cerr << ' ' << result << " create srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " create srk policy" << endl;

	result = Tspi_Policy_SetSecret(policy_srk, TSS_SECRET_MODE_PLAIN, wks_size, wks_blob);
		if(b_debug)	cerr << ' ' << result << " set srk auth" << endl;
		if(b_log)	cerr << ' ' << result << " set srk auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_srk, srk);
		if(b_debug)	cerr << ' ' << result << " assign srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " assign srk policy" << endl;


//BIND OPERATIONS (SET)
	TSS_HKEY		bind;
	TSS_HPOLICY		policy_bind;
	UINT32			init_flags;

	if(!b_1024)
		init_flags = TSS_KEY_TYPE_BIND | TSS_KEY_SIZE_2048  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;
	else
		init_flags = TSS_KEY_TYPE_BIND | TSS_KEY_SIZE_1024  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;

		if(b_debug)	cerr << "Bind Key Section" << endl;
		if(b_log)	clog << "Bind Key Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, init_flags, &bind);
		if(b_debug)	cerr << ' ' << result << " create key object" << endl;
		if(b_log)	cerr << ' ' << result << " create key object" << endl;

	result = Tspi_Context_GetKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_bind, &bind);
		if(b_debug)	cerr << ' ' << result << " load by UUID" << endl;
		if(b_log)	cerr << ' ' << result << " load by UUID" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_bind);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Policy_SetSecret(policy_bind, TSS_SECRET_MODE_PLAIN, bindauth.size, bindauth.blob);
		if(b_debug)	cerr << ' ' << result << " key auth" << endl;
		if(b_log)	cerr << ' ' << result << " key auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_bind, bind);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;

	result = Tspi_Key_LoadKey(bind, srk);
		if(b_debug)	cerr << ' ' << result << " load key" << endl;
		if(b_log)	cerr << ' ' << result << " load key" << endl;


//ENCDATA SECTION
	TSS_HENCDATA		encdata;
	TSS_HPOLICY			policy_encdata;

		if(b_debug)	cerr << "EncData Section" << endl;
		if(b_log)	clog << "EncData Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_ENCDATA, TSS_ENCDATA_BIND, &encdata);
		if(b_debug)	cerr << ' ' << result << " create encdata" << endl;
		if(b_log)	cerr << ' ' << result << " create encdata" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_encdata);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Policy_SetSecret(policy_encdata, TSS_SECRET_MODE_PLAIN, encauth.size, encauth.blob);
		if(b_debug)	cerr << ' ' << result << " encdata auth" << endl;
		if(b_log)	cerr << ' ' << result << " encdata auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_encdata, encdata);
		if(b_debug)	cerr << ' ' << result << " assign encdata authorization" << endl;
		if(b_log)	cerr << ' ' << result << " assign encdata authorization" << endl;


//BIND OPERATIONS
	UINT32			enc_size;
	BYTE*			enc_blob;

		if(b_debug)	cerr << "Bind Section" << endl;
		if(b_log)	clog << "Bind Section" << endl;

	result = Tspi_Data_Bind(encdata, bind, datablob.size, datablob.blob);
		if(b_debug)	cerr << ' ' << result << " BIND" << endl;
		if(b_log)	cerr << ' ' << result << " BIND" << endl;
		return_code = result;

	if(result == 0)
	{
		result = Tspi_GetAttribData(encdata, TSS_TSPATTRIB_ENCDATA_BLOB, TSS_TSPATTRIB_ENCDATABLOB_BLOB, &enc_size, &enc_blob);
			if(b_debug)	cerr << ' ' << result << " get bound data blob" << endl;
			if(b_log)	cerr << ' ' << result << " get bound data blob" << endl;
			return_code = result;

		if(result == 0)
		{
			for(UINT32 i = 0; i < enc_size; i++)
				cout << setw(2) << setfill('0') << setbase(16) << (int)enc_blob[i];
			if(b_debug) cerr << endl;
			if(b_log) clog << endl;

			result = Tspi_Context_FreeMemory(context, enc_blob);
		}
	}


//CLEANUP SECTION
	result = Tspi_Context_CloseObject(context, policy_bind);
	result = Tspi_Context_CloseObject(context, bind);
	result = Tspi_Context_CloseObject(context, policy_encdata);
	result = Tspi_Context_CloseObject(context, encdata);
	result = Tspi_Context_CloseObject(context, srk);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Unbind
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::unbind()
{
	if(b_help)
	{
		cout << "Unbind (" << i_mode << ") --- Decrypts data based on a binding key" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -blob (hex blob, data to decrypt)" << endl;
		cout << " -blob_auth (hex blob, data blob authorization data)" << endl;
		cout << " -key_auth (hex blob, binding key authorization data)" << endl;
		cout << " -key_index (integer, index number for key)" << endl;
		cout << " OPTIONAL PARAMETERS" << endl;
		cout << " -1024 (flag, uses 1024 keys instead of 2048)" << endl;
		cout << " OUTPUTS" << endl;
		cout << " unbound data blob (hex blob, unencrypted data)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	string				s_blob;
	string				s_keyauth;
	string				s_encauth;
	int					i_keyindex = 0;
	bool				b_1024 = false;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-1024") == 0)
		{
			b_1024 = true;
		}

		if(s_argv[i].compare("-blob") == 0)
		{
			if(++i >= i_argc) return;
			s_blob = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_keyauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_index") == 0)
		{
			if(++i >= i_argc) return;
			i_keyindex = atoi(s_argv[i].c_str());
			i_success++;
		}

		if(s_argv[i].compare("-blob_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_encauth = s_argv[i];
			i_success++;
		}
	}
	if(i_success != 4)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	datablob(s_blob);
	NIARL_Util_ByteBlob	bindauth(s_keyauth);
	NIARL_Util_ByteBlob	encauth(s_encauth);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;
	UINT32				wks_size = sizeof(wks_blob);

	TSS_UUID			uuid_bind = TSS_UUID_USK2;
	uuid_bind.rgbNode[5] = (BYTE)i_keyindex;
	uuid_bind.rgbNode[0] = 0x05;


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;


//SRK OPERATIONS (SET)
	TSS_HKEY		srk;
	TSS_HPOLICY		policy_srk;
	TSS_UUID		uuid_srk = TSS_UUID_SRK;

		if(b_debug)	cerr << "SRK Section" << endl;
		if(b_log)	clog << "SRK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_TSP_SRK, &srk);
		if(b_debug)	cerr << ' ' << result << " create srk object" << endl;
		if(b_log)	cerr << ' ' << result << " create srk object" << endl;

	result = Tspi_Context_LoadKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_srk, &srk);
		if(b_debug)	cerr << ' ' << result << " load srk by uuid" << endl;
		if(b_log)	cerr << ' ' << result << " load srk by uuid" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_srk);
		if(b_debug)	cerr << ' ' << result << " create srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " create srk policy" << endl;

	result = Tspi_Policy_SetSecret(policy_srk, TSS_SECRET_MODE_PLAIN, wks_size, wks_blob);
		if(b_debug)	cerr << ' ' << result << " set srk auth" << endl;
		if(b_log)	cerr << ' ' << result << " set srk auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_srk, srk);
		if(b_debug)	cerr << ' ' << result << " assign srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " assign srk policy" << endl;


//BIND OPERATIONS (SET)
	TSS_HKEY		bind;
	TSS_HPOLICY		policy_bind;
	UINT32			init_flags;

	if(!b_1024)
		init_flags = TSS_KEY_TYPE_BIND | TSS_KEY_SIZE_2048  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;
	else
		init_flags = TSS_KEY_TYPE_BIND | TSS_KEY_SIZE_1024  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;

		if(b_debug)	cerr << "Bind Key Section" << endl;
		if(b_log)	clog << "Bind Key Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, init_flags, &bind);
		if(b_debug)	cerr << ' ' << result << " create key object" << endl;
		if(b_log)	cerr << ' ' << result << " create key object" << endl;

	result = Tspi_Context_GetKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_bind, &bind);
		if(b_debug)	cerr << ' ' << result << " load by UUID" << endl;
		if(b_log)	cerr << ' ' << result << " load by UUID" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_bind);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Policy_SetSecret(policy_bind, TSS_SECRET_MODE_PLAIN, bindauth.size, bindauth.blob);
		if(b_debug)	cerr << ' ' << result << " key auth" << endl;
		if(b_log)	cerr << ' ' << result << " key auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_bind, bind);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;

	result = Tspi_Key_LoadKey(bind, srk);
		if(b_debug)	cerr << ' ' << result << " load key" << endl;
		if(b_log)	cerr << ' ' << result << " load key" << endl;


//ENCDATA SECTION
	TSS_HENCDATA		encdata;
	TSS_HPOLICY			policy_encdata;

		if(b_debug)	cerr << "EncData Section" << endl;
		if(b_log)	clog << "EncData Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_ENCDATA, TSS_ENCDATA_BIND, &encdata);
		if(b_debug)	cerr << ' ' << result << " create encdata" << endl;
		if(b_log)	cerr << ' ' << result << " create encdata" << endl;

	result = Tspi_SetAttribData(encdata, TSS_TSPATTRIB_ENCDATA_BLOB, TSS_TSPATTRIB_ENCDATABLOB_BLOB, datablob.size, datablob.blob);
		if(b_debug)	cerr << ' ' << result << " load encrypted blob" << endl;
		if(b_log)	cerr << ' ' << result << " load encrypted blob" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_encdata);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Policy_SetSecret(policy_encdata, TSS_SECRET_MODE_PLAIN, encauth.size, encauth.blob);
		if(b_debug)	cerr << ' ' << result << " encdata auth" << endl;
		if(b_log)	cerr << ' ' << result << " encdata auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_encdata, encdata);
		if(b_debug)	cerr << ' ' << result << " assign encdata authorization" << endl;
		if(b_log)	cerr << ' ' << result << " assign encdata authorization" << endl;


//UNBIND OPERATIONS
	UINT32			enc_size;
	BYTE*			enc_blob;

		if(b_debug)	cerr << "Unbind Section" << endl;
		if(b_log)	clog << "Unbind Section" << endl;

	result = Tspi_Data_Unbind(encdata, bind, &(datablob.size), &(datablob.blob));
		if(b_debug)	cerr << ' ' << result << " UNBIND" << endl;
		if(b_log)	cerr << ' ' << result << " UNBIND" << endl;
		return_code = result;

	if(result == 0)
	{
		result = Tspi_GetAttribData(encdata, TSS_TSPATTRIB_ENCDATA_BLOB, TSS_TSPATTRIB_ENCDATABLOB_BLOB, &enc_size, &enc_blob);
		if(b_debug)	cerr << ' ' << result << " get unbound data blob" << endl;
		if(b_log)	cerr << ' ' << result << " get unbound data blob" << endl;
		return_code = result;

		if(result == 0)
		{
			for(UINT32 i = 0; i < enc_size; i++)
				cout << setw(2) << setfill('0') << setbase(16) << (int)enc_blob[i];
			if(b_debug) cerr << endl;
			if(b_log) clog << endl;

			result = Tspi_Context_FreeMemory(context, enc_blob);
		}
	}


//CLEANUP SECTION
	result = Tspi_Context_CloseObject(context, policy_bind);
	result = Tspi_Context_CloseObject(context, bind);
	result = Tspi_Context_CloseObject(context, policy_encdata);
	result = Tspi_Context_CloseObject(context, encdata);
	result = Tspi_Context_CloseObject(context, srk);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Seal Bind
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::seal_bind()
{
	if(b_help)
	{
		cout << "Seal Bind (" << i_mode << ") --- Encrypts data using a binding key and platform state" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -blob (hex blob, data to encrypt)" << endl;
		cout << " -blob_auth (hex blob, data blob authorization data)" << endl;
		cout << " -key_auth (hex blob, binding key authorization data)" << endl;
		cout << " -key_index (integer, index number for key)" << endl;
		cout << " -mask (hex string, controls PCR index selection)" << endl;
		cout << " OPTIONAL PARAMETERS" << endl;
		cout << " -1024 (flag, uses 1024 keys instead of 2048)" << endl;
		cout << " OUTPUTS" << endl;
		cout << " sealed bound data blob (hex blob, encrypted data)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	string				s_blob;
	string				s_keyauth;
	string				s_pcrs;
	string				s_encauth;
	int					i_keyindex = 0;
	bool				b_1024 = false;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-1024") == 0)
		{
			b_1024 = true;
		}

		if(s_argv[i].compare("-blob") == 0)
		{
			if(++i >= i_argc) return;
			s_blob = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_keyauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_index") == 0)
		{
			if(++i >= i_argc) return;
			i_keyindex = atoi(s_argv[i].c_str());
			i_success++;
		}

		if(s_argv[i].compare("-mask") == 0)
		{
			if(++i >= i_argc) return;
			s_pcrs = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-blob_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_encauth = s_argv[i];
			i_success++;
		}
	}
	if(i_success != 5)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	datablob(s_blob);
	NIARL_Util_ByteBlob	bindauth(s_keyauth);
	NIARL_Util_Mask		pcrmask(s_pcrs);
	NIARL_Util_ByteBlob	encauth(s_encauth);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;
	UINT32				wks_size = sizeof(wks_blob);

	TSS_UUID			uuid_bind = TSS_UUID_USK2;
	uuid_bind.rgbNode[5] = (BYTE)i_keyindex;
	uuid_bind.rgbNode[0] = 0x05;


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;


//SRK OPERATIONS (SET)
	TSS_HKEY		srk;
	TSS_HPOLICY		policy_srk;
	TSS_UUID		uuid_srk = TSS_UUID_SRK;

		if(b_debug)	cerr << "SRK Section" << endl;
		if(b_log)	clog << "SRK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_TSP_SRK, &srk);
		if(b_debug)	cerr << ' ' << result << " create srk object" << endl;
		if(b_log)	cerr << ' ' << result << " create srk object" << endl;

	result = Tspi_Context_LoadKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_srk, &srk);
		if(b_debug)	cerr << ' ' << result << " load srk by uuid" << endl;
		if(b_log)	cerr << ' ' << result << " load srk by uuid" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_srk);
		if(b_debug)	cerr << ' ' << result << " create srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " create srk policy" << endl;

	result = Tspi_Policy_SetSecret(policy_srk, TSS_SECRET_MODE_PLAIN, wks_size, wks_blob);
		if(b_debug)	cerr << ' ' << result << " set srk auth" << endl;
		if(b_log)	cerr << ' ' << result << " set srk auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_srk, srk);
		if(b_debug)	cerr << ' ' << result << " assign srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " assign srk policy" << endl;


//BIND OPERATIONS (SET)
	TSS_HKEY		bind;
	TSS_HPOLICY		policy_bind;
	UINT32			init_flags;

	if(!b_1024)
		init_flags = TSS_KEY_TYPE_BIND | TSS_KEY_SIZE_2048  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;
	else
		init_flags = TSS_KEY_TYPE_BIND | TSS_KEY_SIZE_1024  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;

		if(b_debug)	cerr << "Bind Key Section" << endl;
		if(b_log)	clog << "Bind Key Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, init_flags, &bind);
		if(b_debug)	cerr << ' ' << result << " create key object" << endl;
		if(b_log)	cerr << ' ' << result << " create key object" << endl;

	result = Tspi_Context_GetKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_bind, &bind);
		if(b_debug)	cerr << ' ' << result << " load by UUID" << endl;
		if(b_log)	cerr << ' ' << result << " load by UUID" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_bind);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Policy_SetSecret(policy_bind, TSS_SECRET_MODE_PLAIN, bindauth.size, bindauth.blob);
		if(b_debug)	cerr << ' ' << result << " key auth" << endl;
		if(b_log)	cerr << ' ' << result << " key auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_bind, bind);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;

	result = Tspi_Key_LoadKey(bind, srk);
		if(b_debug)	cerr << ' ' << result << " load key" << endl;
		if(b_log)	cerr << ' ' << result << " load key" << endl;


//ENCDATA SECTION
	TSS_HENCDATA		encdata;
	TSS_HPOLICY			policy_encdata;

		if(b_debug)	cerr << "EncData Section" << endl;
		if(b_log)	clog << "EncData Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_ENCDATA, TSS_ENCDATA_SEAL, &encdata);
		if(b_debug)	cerr << ' ' << result << " create encdata" << endl;
		if(b_log)	cerr << ' ' << result << " create encdata" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_encdata);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Policy_SetSecret(policy_encdata, TSS_SECRET_MODE_PLAIN, encauth.size, encauth.blob);
		if(b_debug)	cerr << ' ' << result << " encdata auth" << endl;
		if(b_log)	cerr << ' ' << result << " encdata auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_encdata, encdata);
		if(b_debug)	cerr << ' ' << result << " assign encdata authorization" << endl;
		if(b_log)	cerr << ' ' << result << " assign encdata authorization" << endl;


//PCR OPERATIONS
	TSS_HPCRS		pcr;
	UINT32			pcr_size;
	BYTE*			pcr_blob;

		if(b_debug)	cerr << "PCR Section" << endl;
		if(b_log)	clog << "PCR Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_PCRS, 0, &pcr);
		if(b_debug)	cerr << ' ' << result << " create pcr object" << endl;
		if(b_log)	cerr << ' ' << result << " create pcr object" << endl;

	for(UINT32 i = 0; i < pcrmask.size; i++)
	{
		result = Tspi_TPM_PcrRead(tpm, pcrmask.index[i], &pcr_size, &pcr_blob);
			if(b_debug)	cerr << ' ' << result << " read pcr index " << pcrmask.index[i] << endl;
			if(b_log)	cerr << ' ' << result << " read pcr index " << pcrmask.index[i] << endl;

		result = Tspi_PcrComposite_SelectPcrIndex(pcr, pcrmask.index[i]);
			if(b_debug)	cerr << ' ' << result << " select composite index" << endl;
			if(b_log)	cerr << ' ' << result << " select composite index" << endl;

		result = Tspi_PcrComposite_SetPcrValue(pcr, pcrmask.index[i], pcr_size, pcr_blob);
			if(b_debug)	cerr << ' ' << result << " set pcr value" << endl;
			if(b_log)	cerr << ' ' << result << " set pcr value" << endl;

//		for(UINT32 j = 0; j < pcr_size; j++)
//			cout << setw(2) << setfill('0') << setbase(16) << (int)pcr_blob[j];
//		if(!b_debug && !b_log) cout << ' ';
//		if(b_debug) cerr << endl;
//		if(b_log) clog << endl;

		result = Tspi_Context_FreeMemory(context, pcr_blob);
			if(b_debug)	cerr << ' ' << result << " clear dynamic array" << endl;
			if(b_log)	cerr << ' ' << result << " clear dynamic array" << endl;
	}


//SEAL OPERATIONS
	UINT32			enc_size;
	BYTE*			enc_blob;

		if(b_debug)	cerr << "Seal Bind Section" << endl;
		if(b_log)	clog << "Seal Bind Section" << endl;

	result = Tspi_Data_Seal(encdata, bind, datablob.size, datablob.blob, pcr);
		if(b_debug)	cerr << ' ' << result << " SEAL BIND" << endl;
		if(b_log)	cerr << ' ' << result << " SEAL BIND" << endl;
		return_code = result;

	if(result == 0)
	{
		result = Tspi_GetAttribData(encdata, TSS_TSPATTRIB_ENCDATA_BLOB, TSS_TSPATTRIB_ENCDATABLOB_BLOB, &enc_size, &enc_blob);
			if(b_debug)	cerr << ' ' << result << " get sealed bound blob" << endl;
			if(b_log)	cerr << ' ' << result << " get sealed bound blob" << endl;
			return_code = result;

		if(result == 0)
		{
			for(UINT32 i = 0; i < enc_size; i++)
				cout << setw(2) << setfill('0') << setbase(16) << (int)enc_blob[i];
			if(b_debug) cerr << endl;
			if(b_log) clog << endl;

			result = Tspi_Context_FreeMemory(context, enc_blob);
		}
	}


//CLEANUP SECTION
	result = Tspi_Context_CloseObject(context, policy_bind);
	result = Tspi_Context_CloseObject(context, bind);
	result = Tspi_Context_CloseObject(context, policy_encdata);
	result = Tspi_Context_CloseObject(context, encdata);
	result = Tspi_Context_CloseObject(context, srk);
	result = Tspi_Context_CloseObject(context, pcr);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Unseal Unbind
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::unseal_unbind()
{
	if(b_help)
	{
		cout << "Unseal Unbind (" << i_mode << ") --- Dencrypts data using a binding key and platform state" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -blob (hex blob, data to decrypt)" << endl;
		cout << " -blob_auth (hex blob, data blob authorization data)" << endl;
		cout << " -key_auth (hex blob, binding key authorization data)" << endl;
		cout << " -key_index (integer, index number for key)" << endl;
		cout << " OPTIONAL PARAMETERS" << endl;
		cout << " -1024 (flag, uses 1024 keys instead of 2048)" << endl;
		cout << " OUTPUTS" << endl;
		cout << " unsealed unbound data blob (hex blob, unencrypted data)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	string				s_blob;
	string				s_keyauth;
	string				s_encauth;
	int					i_keyindex = 0;
	bool				b_1024 = false;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-1024") == 0)
		{
			b_1024 = true;
		}

		if(s_argv[i].compare("-blob") == 0)
		{
			if(++i >= i_argc) return;
			s_blob = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_keyauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_index") == 0)
		{
			if(++i >= i_argc) return;
			i_keyindex = atoi(s_argv[i].c_str());
			i_success++;
		}

		if(s_argv[i].compare("-blob_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_encauth = s_argv[i];
			i_success++;
		}
	}
	if(i_success != 4)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	datablob(s_blob);
	NIARL_Util_ByteBlob	bindauth(s_keyauth);
	NIARL_Util_ByteBlob	encauth(s_encauth);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;
	UINT32				wks_size = sizeof(wks_blob);

	TSS_UUID			uuid_bind = TSS_UUID_USK2;
	uuid_bind.rgbNode[5] = (BYTE)i_keyindex;
	uuid_bind.rgbNode[0] = 0x05;


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;


//SRK OPERATIONS (SET)
	TSS_HKEY		srk;
	TSS_HPOLICY		policy_srk;
	TSS_UUID		uuid_srk = TSS_UUID_SRK;

		if(b_debug)	cerr << "SRK Section" << endl;
		if(b_log)	clog << "SRK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_TSP_SRK, &srk);
		if(b_debug)	cerr << ' ' << result << " create srk object" << endl;
		if(b_log)	cerr << ' ' << result << " create srk object" << endl;

	result = Tspi_Context_LoadKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_srk, &srk);
		if(b_debug)	cerr << ' ' << result << " load srk by uuid" << endl;
		if(b_log)	cerr << ' ' << result << " load srk by uuid" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_srk);
		if(b_debug)	cerr << ' ' << result << " create srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " create srk policy" << endl;

	result = Tspi_Policy_SetSecret(policy_srk, TSS_SECRET_MODE_PLAIN, wks_size, wks_blob);
		if(b_debug)	cerr << ' ' << result << " set srk auth" << endl;
		if(b_log)	cerr << ' ' << result << " set srk auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_srk, srk);
		if(b_debug)	cerr << ' ' << result << " assign srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " assign srk policy" << endl;


//BIND OPERATIONS (SET)
	TSS_HKEY		bind;
	TSS_HPOLICY		policy_bind;
	UINT32			init_flags;

	if(!b_1024)
		init_flags = TSS_KEY_TYPE_BIND | TSS_KEY_SIZE_2048  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;
	else
		init_flags = TSS_KEY_TYPE_BIND | TSS_KEY_SIZE_1024  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;

		if(b_debug)	cerr << "Bind Key Section" << endl;
		if(b_log)	clog << "Bind Key Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, init_flags, &bind);
		if(b_debug)	cerr << ' ' << result << " create key object" << endl;
		if(b_log)	cerr << ' ' << result << " create key object" << endl;

	result = Tspi_Context_GetKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_bind, &bind);
		if(b_debug)	cerr << ' ' << result << " load by UUID" << endl;
		if(b_log)	cerr << ' ' << result << " load by UUID" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_bind);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Policy_SetSecret(policy_bind, TSS_SECRET_MODE_PLAIN, bindauth.size, bindauth.blob);
		if(b_debug)	cerr << ' ' << result << " key auth" << endl;
		if(b_log)	cerr << ' ' << result << " key auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_bind, bind);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;

	result = Tspi_Key_LoadKey(bind, srk);
		if(b_debug)	cerr << ' ' << result << " load key" << endl;
		if(b_log)	cerr << ' ' << result << " load key" << endl;


//ENCDATA SECTION
	TSS_HENCDATA		encdata;
	TSS_HPOLICY			policy_encdata;

		if(b_debug)	cerr << "EncData Section" << endl;
		if(b_log)	clog << "EncData Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_ENCDATA, TSS_ENCDATA_BIND, &encdata);
		if(b_debug)	cerr << ' ' << result << " create encdata" << endl;
		if(b_log)	cerr << ' ' << result << " create encdata" << endl;

	result = Tspi_SetAttribData(encdata, TSS_TSPATTRIB_ENCDATA_BLOB, TSS_TSPATTRIB_ENCDATABLOB_BLOB, datablob.size, datablob.blob);
		if(b_debug)	cerr << ' ' << result << " load encrypted blob" << endl;
		if(b_log)	cerr << ' ' << result << " load encrypted blob" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_encdata);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Policy_SetSecret(policy_encdata, TSS_SECRET_MODE_PLAIN, encauth.size, encauth.blob);
		if(b_debug)	cerr << ' ' << result << " encdata auth" << endl;
		if(b_log)	cerr << ' ' << result << " encdata auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_encdata, encdata);
		if(b_debug)	cerr << ' ' << result << " assign encdata authorization" << endl;
		if(b_log)	cerr << ' ' << result << " assign encdata authorization" << endl;


//UNSEAL OPERATIONS
	UINT32			enc_size;
	BYTE*			enc_blob;

		if(b_debug)	cerr << "Unseal Unbind Section" << endl;
		if(b_log)	clog << "Unseal Unbind Section" << endl;

	result = Tspi_SetAttribData(encdata, TSS_TSPATTRIB_ENCDATA_BLOB, TSS_TSPATTRIB_ENCDATABLOB_BLOB, datablob.size, datablob.blob);
		if(b_debug)	cerr << ' ' << result << " UNSEAL UNBIND" << endl;
		if(b_log)	cerr << ' ' << result << " UNSEAL UNBIND" << endl;

	result = Tspi_Data_Unseal(encdata, bind, &enc_size, &enc_blob);
		if(b_debug)	cerr << ' ' << result << " get unsealed unbound data blob" << endl;
		if(b_log)	cerr << ' ' << result << " get unsealed unbound data blob" << endl;
		return_code = result;

	if(result == 0)
	{
		for(UINT32 i = 0; i < enc_size; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)enc_blob[i];
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		result = Tspi_Context_FreeMemory(context, enc_blob);
	}


//CLEANUP SECTION
	result = Tspi_Context_CloseObject(context, policy_bind);
	result = Tspi_Context_CloseObject(context, bind);
	result = Tspi_Context_CloseObject(context, policy_encdata);
	result = Tspi_Context_CloseObject(context, encdata);
	result = Tspi_Context_CloseObject(context, srk);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Get Random Integer
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::get_rand_int()
{
	if(b_help)
	{
		cout << "Get Random Integer (" << i_mode << ") --- Generates a random positive number" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -bytes (positive integer, max random number size in bytes)" << endl;
		cout << " OUTPUTS" << endl;
		cout << " integer (integer, random number)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	UINT32				numbytes = 0;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-bytes") == 0)
		{
			if(++i >= i_argc) return;
			numbytes = atoi(s_argv[i].c_str());
			i_success++;
		}
	}
	if(i_success != 1)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;


//GET RANDOM NUMBER
	BYTE*		randbytes;

		if(b_debug)	cerr << "Get Random Section" << endl;
		if(b_log)	clog << "Get Random Section" << endl;

	result = Tspi_TPM_GetRandom(tpm, numbytes, &randbytes);
		if(b_debug)	cerr << ' ' << result << " GET RANDOM" << endl;
		if(b_log)	cerr << ' ' << result << " GET RANDOM" << endl;
		return_code = result;

	for(UINT32 i = 0; i < numbytes; i++)
	{
		cout << setbase(16) << setw(2) << setfill('0') << (int)randbytes[i];
	}

//CLEANUP SECTION
	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Sign
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::sign()
{
	if(b_help)
	{
		cout << "Sign (" << i_mode << ") --- Signs a data blob" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -blob (hex blob, data to sign)" << endl;
		cout << " -key_auth (hex blob, key authorization data)" << endl;
		cout << " -key_index (integer, index number for key)" << endl;
		cout << " OPTIONAL PARAMETERS" << endl;
		cout << " -1024 (flag, uses 1024 keys instead of 2048)" << endl;
		cout << " OUTPUTS" << endl;
		cout << " signature (hex blob, signature)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	string				s_blob;
	string				s_keyauth;
	int					i_keyindex = 0;
	bool				b_1024 = false;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-1024") == 0)
		{
			b_1024 = true;
		}

		if(s_argv[i].compare("-blob") == 0)
		{
			if(++i >= i_argc) return;
			s_blob = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_keyauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_index") == 0)
		{
			if(++i >= i_argc) return;
			i_keyindex = atoi(s_argv[i].c_str());
			i_success++;
		}
	}
	if(i_success != 3)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	datablob(s_blob);
	NIARL_Util_ByteBlob	signauth(s_keyauth);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;
	UINT32				wks_size = sizeof(wks_blob);

	TSS_UUID			uuid_sign = TSS_UUID_USK2;
	uuid_sign.rgbNode[5] = (BYTE)i_keyindex;
	uuid_sign.rgbNode[0] = 0x06;


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;


//SRK OPERATIONS (SET)
	TSS_HKEY		srk;
	TSS_HPOLICY		policy_srk;
	TSS_UUID		uuid_srk = TSS_UUID_SRK;

		if(b_debug)	cerr << "SRK Section" << endl;
		if(b_log)	clog << "SRK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_TSP_SRK, &srk);
		if(b_debug)	cerr << ' ' << result << " create srk object" << endl;
		if(b_log)	cerr << ' ' << result << " create srk object" << endl;

	result = Tspi_Context_LoadKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_srk, &srk);
		if(b_debug)	cerr << ' ' << result << " load srk by uuid" << endl;
		if(b_log)	cerr << ' ' << result << " load srk by uuid" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_srk);
		if(b_debug)	cerr << ' ' << result << " create srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " create srk policy" << endl;

	result = Tspi_Policy_SetSecret(policy_srk, TSS_SECRET_MODE_PLAIN, wks_size, wks_blob);
		if(b_debug)	cerr << ' ' << result << " set srk auth" << endl;
		if(b_log)	cerr << ' ' << result << " set srk auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_srk, srk);
		if(b_debug)	cerr << ' ' << result << " assign srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " assign srk policy" << endl;


//SIGNING OPERATIONS (SET)
	TSS_HKEY		sign;
	TSS_HPOLICY		policy_sign;
	UINT32			init_flags;

	if(!b_1024)
		init_flags = TSS_KEY_TYPE_SIGNING | TSS_KEY_SIZE_2048  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;
	else
		init_flags = TSS_KEY_TYPE_SIGNING | TSS_KEY_SIZE_1024  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;


		if(b_debug)	cerr << "Signing Key Section" << endl;
		if(b_log)	clog << "Signing Key Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, init_flags, &sign);
		if(b_debug)	cerr << ' ' << result << " create key object" << endl;
		if(b_log)	cerr << ' ' << result << " create key object" << endl;

	result = Tspi_Context_GetKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_sign, &sign);
		if(b_debug)	cerr << ' ' << result << " load by UUID" << endl;
		if(b_log)	cerr << ' ' << result << " load by UUID" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_sign);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Policy_SetSecret(policy_sign, TSS_SECRET_MODE_PLAIN, signauth.size, signauth.blob);
		if(b_debug)	cerr << ' ' << result << " key auth" << endl;
		if(b_log)	cerr << ' ' << result << " key auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_sign, sign);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;

	result = Tspi_Key_LoadKey(sign, srk);
		if(b_debug)	cerr << ' ' << result << " load key" << endl;
		if(b_log)	cerr << ' ' << result << " load key" << endl;


//HASH SECTION
	TSS_HHASH		hash;

		if(b_debug)	cerr << "Hash Section" << endl;
		if(b_log)	clog << "Hash Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_HASH, TSS_HASH_SHA1, &hash);
		if(b_debug)	cerr << ' ' << result << " create hash object" << endl;
		if(b_log)	cerr << ' ' << result << " create hash object" << endl;

	result = Tspi_Hash_UpdateHashValue(hash, datablob.size, datablob.blob);
		if(b_debug)	cerr << ' ' << result << " update hash value" << endl;
		if(b_log)	cerr << ' ' << result << " update hash value" << endl;


//SIGNING OPERATIONS
	UINT32			sig_size;
	BYTE*			sig_blob;

		if(b_debug)	cerr << "Sign Section" << endl;
		if(b_log)	clog << "Sign Section" << endl;

	result = Tspi_Hash_Sign(hash, sign, &sig_size, &sig_blob);
		if(b_debug)	cerr << ' ' << result << " SIGN" << endl;
		if(b_log)	cerr << ' ' << result << " SIGN" << endl;
		return_code = result;

	if(result == 0)
	{
		for(UINT32 i = 0; i < sig_size; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)sig_blob[i];
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		result = Tspi_Context_FreeMemory(context, sig_blob);
	}


//CLEANUP SECTION
	result = Tspi_Context_CloseObject(context, policy_sign);
	result = Tspi_Context_CloseObject(context, sign);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Create EK
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::create_ek()
{
	if(b_help)
	{
		cout << "Create Endorsement Key (" << i_mode << ") --- Creates a default endorsement key in the absence of a manufacturer endorsement key" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -nonce (hex blob, anti-replay nonce)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	string				s_nonce;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-nonce") == 0)
		{
			if(++i >= i_argc) return;
			s_nonce = s_argv[i];
			i_success++;
		}
	}
	if(i_success != 1)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	nonce(s_nonce);


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;


//EK SECTION
	TSS_HKEY		ek;
	TSS_VALIDATION	validation;

		if(b_debug)	cerr << "EK Section" << endl;
		if(b_log)	clog << "EK Section" << endl;

	memset(&validation, 0, sizeof(TSS_VALIDATION));
	validation.versionInfo.bMajor = 0x01;
	validation.versionInfo.bMinor = 0x02;
	validation.versionInfo.bRevMajor = 0x01;
	validation.versionInfo.bRevMinor = 0x25;
	validation.ulExternalDataLength = sizeof(TSS_NONCE);
	validation.rgbExternalData = nonce.blob;
	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_SIZE_DEFAULT, &ek);
		if(b_debug)	cerr << ' ' << result << " create ek object" << endl;
		if(b_log)	cerr << ' ' << result << " create ek object" << endl;


//TAKE OWNERSHIP
		if(b_debug)	cerr << "Create EK Section" << endl;
		if(b_log)	clog << "Create EK Section" << endl;

	result = Tspi_TPM_CreateEndorsementKey(tpm, ek, &validation);
		if(b_debug)	cerr << ' ' << result << " CREATE EK" << endl;
		if(b_log)	cerr << ' ' << result << " CREATE EK" << endl;
	return_code = result;

	if(result == 0)
	{
		for(UINT32 i = 0; i < validation.ulExternalDataLength; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)validation.rgbExternalData[i];

		if(!b_debug && !b_log) cout << ' ';
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		for(UINT32 i = 0; i < validation.ulDataLength; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)validation.rgbData[i];

		if(!b_debug && !b_log) cout << ' ';
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		for(UINT32 i = 0; i < validation.ulValidationDataLength; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)validation.rgbValidationData[i];
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;
	}


//CLEANUP
	result = Tspi_Context_FreeMemory(context, validation.rgbData);
	result = Tspi_Context_FreeMemory(context, validation.rgbValidationData);
	result = Tspi_Context_FreeMemory(context, validation.rgbExternalData);

	result = Tspi_Context_CloseObject(context, ek);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

/**********************************************************************************************
	 Quote2
 **********************************************************************************************/

void NIARL_TPM_ModuleV2::quote2()
{
	if(b_help)
	{
		cout << "Quote2 (" << i_mode << ") --- Provides a system integrity quote with signature" << endl;
		cout << " REQUIRED PARAMETERS" << endl;
		cout << " -key_auth (hex blob, identity key authorization data)" << endl;
		cout << " -nonce (hex blob, anti-replay nonce)" << endl;
		cout << " -mask (hex string, controls PCR index selection)" << endl;
		cout << " -key_index (integer, index number for key)" << endl;
		cout << " OUTPUTS" << endl;
		cout << " quote (hex blob, quote digest)" << endl;
		cout << " signature (hex blob, quote signature)" << endl;
		cout << " version_info (hex blob, TCPA_VERSION_INFO)" << endl;
		return_code = -1 * ERROR_ARG_HELP;
		return;
	}


//DYNAMIC CONTENT
	short				i_success = 0;
	string				s_pcrs;
	string				s_aikauth;
	string				s_nonce;
	int					i_keyindex = 0;

	for(short i = 0; i < i_argc; i++)
	{
		if(s_argv[i].compare("-nonce") == 0)
		{
			if(++i >= i_argc) return;
			s_nonce = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_auth") == 0)
		{
			if(++i >= i_argc) return;
			s_aikauth = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-mask") == 0)
		{
			if(++i >= i_argc) return;
			s_pcrs = s_argv[i];
			i_success++;
		}

		if(s_argv[i].compare("-key_index") == 0)
		{
			if(++i >= i_argc) return;
			i_keyindex = atoi(s_argv[i].c_str());
			i_success++;
		}
	}
	if(i_success != 4)
	{
		return_code = -1 * ERROR_ARG_MISSING;
		return;
	}

	NIARL_Util_ByteBlob	aikauth(s_aikauth);
	NIARL_Util_ByteBlob	nonce(s_nonce);
	NIARL_Util_Mask		pcrmask(s_pcrs);
	BYTE				wks_blob[] = TSS_WELL_KNOWN_SECRET;
	UINT32				wks_size = sizeof(wks_blob);

	TSS_UUID			uuid_aik = TSS_UUID_USK2;
	uuid_aik.rgbNode[5] = (BYTE)i_keyindex;
	uuid_aik.rgbNode[0] = 0x04;


//CONTEXT SECTION
	TSS_RESULT		result;
	TSS_HCONTEXT	context;
	TSS_HPOLICY		policy_default;

		if(b_debug)	cerr << "Context Section" << endl;
		if(b_log)	clog << "Context Section" << endl;

	result = Tspi_Context_Create(&context);
		if(b_debug)	cerr << ' ' << result << " create context" << endl;
		if(b_log)	cerr << ' ' << result << " create context" << endl;

	result = Tspi_Context_Connect(context, NULL);
		if(b_debug)	cerr << ' ' << result << " create policy" << endl;
		if(b_log)	cerr << ' ' << result << " create policy" << endl;

	result = Tspi_Context_GetDefaultPolicy(context, &policy_default);
		if(b_debug)	cerr << ' ' << result << " default policy" << endl;
		if(b_log)	cerr << ' ' << result << " default policy" << endl;


//TPM SECTION
	TSS_HTPM		tpm;

		if(b_debug)	cerr << "TPM Section" << endl;
		if(b_log)	clog << "TPM Section" << endl;

	result = Tspi_Context_GetTpmObject(context, &tpm);
		if(b_debug)	cerr << ' ' << result << " tpm context" << endl;
		if(b_log)	cerr << ' ' << result << " tpm context" << endl;


//SRK OPERATIONS (SET)
	TSS_HKEY		srk;
	TSS_HPOLICY		policy_srk;
	TSS_UUID		uuid_srk = TSS_UUID_SRK;

		if(b_debug)	cerr << "SRK Section" << endl;
		if(b_log)	clog << "SRK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_TSP_SRK, &srk);
		if(b_debug)	cerr << ' ' << result << " create srk object" << endl;
		if(b_log)	cerr << ' ' << result << " create srk object" << endl;

	result = Tspi_Context_LoadKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_srk, &srk);
		if(b_debug)	cerr << ' ' << result << " load srk by uuid" << endl;
		if(b_log)	cerr << ' ' << result << " load srk by uuid" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_srk);
		if(b_debug)	cerr << ' ' << result << " create srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " create srk policy" << endl;

	result = Tspi_Policy_SetSecret(policy_srk, TSS_SECRET_MODE_PLAIN, wks_size, wks_blob);
		if(b_debug)	cerr << ' ' << result << " set srk auth" << endl;
		if(b_log)	cerr << ' ' << result << " set srk auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_srk, srk);
		if(b_debug)	cerr << ' ' << result << " assign srk policy" << endl;
		if(b_log)	cerr << ' ' << result << " assign srk policy" << endl;


//AIK OPERATIONS (SET)
	TSS_HKEY		aik;
	TSS_HPOLICY		policy_aik;
	UINT32			init_flags	= TSS_KEY_TYPE_IDENTITY | TSS_KEY_SIZE_2048  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;

		if(b_debug)	cerr << "AIK Section" << endl;
		if(b_log)	clog << "AIK Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_RSAKEY, init_flags, &aik);
		if(b_debug)	cerr << ' ' << result << " create aik object" << endl;
		if(b_log)	cerr << ' ' << result << " create aik object" << endl;

	result = Tspi_Context_GetKeyByUUID(context, TSS_PS_TYPE_SYSTEM, uuid_aik, &aik);
		if(b_debug)	cerr << ' ' << result << " get uuid" << endl;
		if(b_log)	cerr << ' ' << result << " get uuid" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &policy_aik);
		if(b_debug)	cerr << ' ' << result << " create aik policy" << endl;
		if(b_log)	cerr << ' ' << result << " create aik policy" << endl;

	result = Tspi_Policy_SetSecret(policy_aik, TSS_SECRET_MODE_PLAIN, aikauth.size, aikauth.blob);
		if(b_debug)	cerr << ' ' << result << " set aik auth" << endl;
		if(b_log)	cerr << ' ' << result << " set aik auth" << endl;

	result = Tspi_Policy_AssignToObject(policy_aik, aik);
		if(b_debug)	cerr << ' ' << result << " assign" << endl;
		if(b_log)	cerr << ' ' << result << " assign" << endl;

	result = Tspi_Key_LoadKey(aik, srk);
		if(b_debug)	cerr << ' ' << result << " load aik" << endl;
		if(b_log)	cerr << ' ' << result << " load aik" << endl;


//PCR OPERATIONS
	TSS_HPCRS		pcr;
	UINT32			pcr_size;
	BYTE*			pcr_blob;

		if(b_debug)	cerr << "PCR Section" << endl;
		if(b_log)	clog << "PCR Section" << endl;

	result = Tspi_Context_CreateObject(context, TSS_OBJECT_TYPE_PCRS, TSS_PCRS_STRUCT_INFO_SHORT, &pcr);
		if(b_debug)	cerr << ' ' << result << " create pcr object" << endl;
		if(b_log)	cerr << ' ' << result << " create pcr object" << endl;

	for(UINT32 i = 0; i < pcrmask.size; i++)
	{
		result = Tspi_TPM_PcrRead(tpm, pcrmask.index[i], &pcr_size, &pcr_blob);
			if(b_debug)	cerr << ' ' << result << " read pcr index " << pcrmask.index[i] << endl;
			if(b_log)	cerr << ' ' << result << " read pcr index " << pcrmask.index[i] << endl;

		result = Tspi_PcrComposite_SelectPcrIndexEx(pcr, pcrmask.index[i], TSS_PCRS_DIRECTION_RELEASE);
			if(b_debug)	cerr << ' ' << result << " select composite index" << endl;
			if(b_log)	cerr << ' ' << result << " select composite index" << endl;

		result = Tspi_PcrComposite_SetPcrValue(pcr, pcrmask.index[i], pcr_size, pcr_blob);
			if(b_debug)	cerr << ' ' << result << " set pcr value" << endl;
			if(b_log)	cerr << ' ' << result << " set pcr value" << endl;

		for(UINT32 j = 0; j < pcr_size; j++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)pcr_blob[j];
		if(!b_debug && !b_log) cout << ' ';
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		result = Tspi_Context_FreeMemory(context, pcr_blob);
			if(b_debug)	cerr << ' ' << result << " clear dynamic array" << endl;
			if(b_log)	cerr << ' ' << result << " clear dynamic array" << endl;
	}


//QUOTE OPERATIONS
	TSS_VALIDATION	validation;

		if(b_debug)	cerr << "Quote Section" << endl;
		if(b_log)	clog << "Quote Section" << endl;

	memset(&validation, 0, sizeof(TSS_VALIDATION));
	validation.versionInfo.bMajor = 0x01;
	validation.versionInfo.bMinor = 0x02;
	validation.versionInfo.bRevMajor = 0x01;
	validation.versionInfo.bRevMinor = 0x25;
	validation.ulExternalDataLength = sizeof(TSS_NONCE);
	validation.rgbExternalData = nonce.blob;

	BYTE*	versionvalue;
	UINT32	versionsize;

	result = Tspi_TPM_Quote2(tpm, aik, FALSE, pcr, &validation, &versionsize, &versionvalue);
		if(b_debug)	cerr << ' ' << result << " QUOTE" << endl;
		if(b_log)	cerr << ' ' << result << " QUOTE" << endl;
		return_code = result;

	if(result == 0)
	{
		for(UINT32 i = 0; i < validation.ulExternalDataLength; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)validation.rgbExternalData[i];

		if(!b_debug && !b_log) cout << ' ';
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		for(UINT32 i = 0; i < validation.ulDataLength; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)validation.rgbData[i];

		if(!b_debug && !b_log) cout << ' ';
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		for(UINT32 i = 0; i < validation.ulValidationDataLength; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)validation.rgbValidationData[i];

		if(!b_debug && !b_log) cout << ' ';
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		for(UINT32 i = 0; i < versionsize; i++)
			cout << setw(2) << setfill('0') << setbase(16) << (int)versionvalue[i];
		if(b_debug) cerr << endl;
		if(b_log) clog << endl;

		delete [] versionvalue;
	}


//CLEANUP SECTION
	result = Tspi_Context_FreeMemory(context, validation.rgbData);
	result = Tspi_Context_FreeMemory(context, validation.rgbValidationData);
	result = Tspi_Context_FreeMemory(context, validation.rgbExternalData);

	result = Tspi_Context_CloseObject(context, policy_aik);
	result = Tspi_Context_CloseObject(context, aik);
	result = Tspi_Context_CloseObject(context, srk);
	result = Tspi_Context_CloseObject(context, pcr);

	//result = Tspi_Context_FreeMemory(context, NULL);
	result = Tspi_Context_Close(context);

	return;
}

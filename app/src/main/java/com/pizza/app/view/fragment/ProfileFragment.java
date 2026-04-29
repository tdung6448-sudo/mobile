package com.pizza.app.view.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.pizza.app.databinding.FragmentProfileBinding;
import com.pizza.app.util.GlideHelper;
import com.pizza.app.view.activity.LoginActivity;
import com.pizza.app.viewmodel.AuthViewModel;
import com.pizza.app.viewmodel.ProfileViewModel;

/**
 * Hồ sơ cá nhân — xem, chỉnh sửa thông tin, đăng xuất
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private AuthViewModel          authViewModel;
    private ProfileViewModel       profileViewModel;
    private String                 currentUid;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) uploadAvatar(uri);
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding          = FragmentProfileBinding.inflate(inflater, container, false);
        authViewModel    = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null) return;

        loadUserProfile();
        setupClickListeners();
    }

    private void loadUserProfile() {
        profileViewModel.getUser(currentUid).observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.data != null) {
                com.pizza.app.model.User user = result.data;
                binding.tvName.setText(user.getName());
                binding.tvEmail.setText(user.getEmail());
                binding.tvPhone.setText(user.getPhone() != null ? user.getPhone() : "Chưa có SĐT");
                GlideHelper.loadAvatar(binding.ivAvatar, user.getAvatar());
            }
        });
    }

    private void setupClickListeners() {
        // Đổi avatar
        binding.ivAvatar.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*"));

        // Chỉnh sửa tên
        binding.layoutEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // Quản lý địa chỉ
        binding.layoutAddresses.setOnClickListener(v ->
                startActivity(new Intent(requireContext(),
                        com.pizza.app.view.activity.AddressManageActivity.class)));

        // Chat với cửa hàng
        binding.layoutChat.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(),
                    com.pizza.app.view.activity.ChatActivity.class);
            intent.putExtra(com.pizza.app.util.Constants.EXTRA_CHAT_ID, currentUid);
            startActivity(intent);
        });

        // Đổi mật khẩu
        binding.layoutChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        // Đăng xuất
        binding.layoutLogout.setOnClickListener(v -> showLogoutDialog());

        // Xóa tài khoản
        binding.layoutDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void uploadAvatar(Uri uri) {
        authViewModel.uploadAvatar(currentUid, uri).observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                GlideHelper.loadAvatar(binding.ivAvatar, result.data);
                Toast.makeText(requireContext(), "Cập nhật ảnh đại diện thành công", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditProfileDialog() {
        View dialogView = getLayoutInflater().inflate(
                com.pizza.app.R.layout.dialog_edit_profile, null);
        com.google.android.material.textfield.TextInputEditText etName =
                dialogView.findViewById(com.pizza.app.R.id.etName);
        com.google.android.material.textfield.TextInputEditText etPhone =
                dialogView.findViewById(com.pizza.app.R.id.etPhone);

        etName.setText(binding.tvName.getText());
        etPhone.setText(binding.tvPhone.getText());

        new AlertDialog.Builder(requireContext())
                .setTitle("Chỉnh sửa thông tin")
                .setView(dialogView)
                .setPositiveButton("Lưu", (d, w) -> {
                    String name  = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    profileViewModel.updateProfile(currentUid, name, phone)
                            .observe(getViewLifecycleOwner(), result -> {
                                if (result.isSuccess()) {
                                    Toast.makeText(requireContext(), "Đã cập nhật", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void showChangePasswordDialog() {
        // Delegate sang ChangePasswordActivity để xử lý riêng biệt
        startActivity(new Intent(requireContext(),
                com.pizza.app.view.activity.ChangePasswordActivity.class));
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (d, w) -> {
                    authViewModel.logout();
                    new com.pizza.app.util.SharedPrefsHelper(requireContext()).clearAll();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa tài khoản")
                .setMessage("Thao tác này không thể hoàn tác. Bạn có chắc muốn xóa tài khoản?")
                .setPositiveButton("Xóa", (d, w) ->
                        authViewModel.deleteAccount().observe(getViewLifecycleOwner(), result -> {
                            if (result.isSuccess()) {
                                Intent intent = new Intent(requireContext(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
